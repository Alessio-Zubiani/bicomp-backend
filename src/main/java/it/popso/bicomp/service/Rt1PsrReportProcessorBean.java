package it.popso.bicomp.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.minio.messages.Item;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.model.NotificationLevelEnum;
import it.popso.bicomp.utils.BicompConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@Scope("prototype")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class Rt1PsrReportProcessorBean extends ReportProcessor {
	
	private final Rt1Service rt1Service;
	
	public Rt1PsrReportProcessorBean(Rt1Service rt1Service, NotificationService notificationService, final TimerService timerService) {
		super(timerService, notificationService);
		this.rt1Service = rt1Service;
	}
	
	
	@Override
	public void execute() throws IOException {
		
		log.info("START processing PSR RT1 report");
		try {
			List<Item> list = this.rt1Service.getPsrReport();
			if(!list.isEmpty()) {
				this.rt1Service.processPreSettlementReport(list);
				this.rt1Service.moveToBackupFolder(list);
				
				this.buildNotification(this.getClass().getName(), "Successfully loaded following files: ".concat(this.listItemToString(list)), NotificationLevelEnum.INFO.name());
			}
			else {
				log.info("No PSR report to process");
			}
			
			this.setLastExecutionStatus("SUCCESSFULL");
			log.info("END processing PSR RT1 report");
			list.clear();
		}
		catch (BicompException | DataIntegrityViolationException | IOException | ParseException | JpaSystemException | FileManagerException e) {
			log.error(e.toString());
			this.setLastExecutionStatus(BicompConstants.FAILURE);
			log.debug("Updating last execution status to [FAILURE]");
			
			this.buildErrorNotification(this.getClass().getName(), this.getClass().getName(), NotificationLevelEnum.ERROR.name(), e);
		}
	}

}
