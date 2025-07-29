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
public class TipsCamt053ReportProcessorBean extends ReportProcessor {
	
	private final TipsService tipsService;
	
	public TipsCamt053ReportProcessorBean(TipsService tipsService, NotificationService notificationService, final TimerService timerService) {
		super(timerService, notificationService);
		this.tipsService = tipsService;
	}

	
	@Override
	public void execute() throws IOException {
		
		log.info("START processing report TIPS camt.053");		
		try {
			List<Item> list = this.tipsService.getCamt053Report();
			if(!list.isEmpty()) {
				this.tipsService.processCamt053Report(list);
				this.tipsService.moveToBackupFolder053(list);
				
				this.buildNotification(this.getClass().getName(), "Successfully loaded following files: ".concat(this.listItemToString(list)), NotificationLevelEnum.INFO.name());
			}
			else {
				log.info("No CAMT.053 report to process");
			}

			this.setLastExecutionStatus("SUCCESSFULL");
			log.info("END processing report TIPS camt.053");
			list.clear();
		}
		catch (BicompException | JAXBException | DataIntegrityViolationException | IOException | JpaSystemException e) {
			log.error(e.toString());
			this.setLastExecutionStatus(BicompConstants.FAILURE);
			log.debug("Updating last execution status to [FAILURE]");
			
			this.buildErrorNotification(this.getClass().getName(), this.getClass().getName(), NotificationLevelEnum.ERROR.name(), e);
		}
	}

}
