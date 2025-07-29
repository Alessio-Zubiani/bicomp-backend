package it.popso.bicomp.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.model.NotificationLevelEnum;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import it.popso.bicomp.utils.BicompConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@Scope("prototype")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class TipsPaymentExtractorBean extends ReportProcessor {
	
	private final TipsService tipsService;
	
	public TipsPaymentExtractorBean(TipsService tipsService, NotificationService notificationService, final TimerService timerService) {
		super(timerService, notificationService);
		this.tipsService = tipsService;
	}

	
	@Override
	public void execute() throws IOException {
		
		log.info("START extracting TIPS payments");		
		try {
			Date currentDate = LocalDate.now().toDate();
			List<TipsCamt053BankAccountStatementEntry> list = this.tipsService.extractTipsPayment(currentDate);
			
			String fileName = this.tipsService.createTipsPaymentFile(currentDate, list);
			this.buildNotification(this.getClass().getName(), "Successfully created file: ".concat(fileName), NotificationLevelEnum.INFO.name());
			
			this.setLastExecutionStatus("SUCCESSFULL");
			log.info("END extracting TIPS payments");
			list.clear();
		}
		catch (BicompException | ParseException e) {
			log.error(e.toString());
			this.setLastExecutionStatus(BicompConstants.FAILURE);
			log.debug("Updating last execution status to [FAILURE]");
			
			this.buildErrorNotification(this.getClass().getName(), this.getClass().getName(), NotificationLevelEnum.ERROR.name(), e);
		}
	}

}
