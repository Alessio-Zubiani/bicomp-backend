package it.popso.bicomp.service;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.minio.messages.Item;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.model.NotificationLevelEnum;
import it.popso.bicomp.utils.BicompConstants;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@Scope("prototype")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class Rt1RsfReportProcessorBean extends ReportProcessor {
	
	private final Rt1Service rt1Service;
	
	public Rt1RsfReportProcessorBean(Rt1Service rt1Service, NotificationService notificationService, final TimerService timerService) {
		super(timerService, notificationService);
		this.rt1Service = rt1Service;
	}
	
	
	@Override
	public void execute() throws IOException {
		
		log.info("START processing RSF RT1 report");
		try {
			List<Item> list = this.rt1Service.getBulkReport();
			if(!list.isEmpty()) {
				this.rt1Service.processBulkReport(list);
				this.rt1Service.moveToBackupFolder(list);
				
				this.buildNotification(this.getClass().getName(), "Successfully loaded following files: ".concat(this.listItemToString(list)), NotificationLevelEnum.INFO.name());
			}
			else {
				log.info("No RSF report to process");
			}
			
			this.setLastExecutionStatus("SUCCESSFULL");
			log.info("END processing RSF RT1 report");
			list.clear();
		}
		catch (BicompException | DataIntegrityViolationException | IOException | JAXBException | JpaSystemException e) {
			log.error(e.toString());
			this.setLastExecutionStatus(BicompConstants.FAILURE);
			log.debug("Updating last execution status to [FAILURE]");
			
			this.buildErrorNotification(this.getClass().getName(), this.getClass().getName(), NotificationLevelEnum.ERROR.name(), e);
		}
	}

}
