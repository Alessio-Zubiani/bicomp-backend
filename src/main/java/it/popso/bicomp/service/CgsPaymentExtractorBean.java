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
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;
import it.popso.bicomp.model.NotificationLevelEnum;
import it.popso.bicomp.utils.BicompConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@Scope("prototype")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsPaymentExtractorBean extends ReportProcessor {
	
	private final CgsService cgsService;
	
	public CgsPaymentExtractorBean(CgsService cgsService, NotificationService notificationService, final TimerService timerService) {
		super(timerService, notificationService);
		this.cgsService = cgsService;
	}

	
	@Override
	public void execute() throws IOException {
		
		log.info("START extracting CGS payments");		
		try {
			Date currentDate = LocalDate.now().toDate();
			List<CgsLmrLtinBulkStatementEntry> ltList = this.cgsService.extractCgsLiquidityTransfer(currentDate);
			List<CgsLmrPlcrBulkStatementEntry> paymentList = this.cgsService.extractCgsPayment(currentDate);
			
			String fileName = this.cgsService.createCgsPaymentFile(currentDate, ltList, paymentList);
			this.buildNotification(this.getClass().getName(), "Successfully created file: ".concat(fileName), NotificationLevelEnum.INFO.name());

			this.setLastExecutionStatus("SUCCESSFULL");
			log.info("END extracting CGS payments");
			ltList.clear();
			paymentList.clear();
		}
		catch (BicompException | IOException | ParseException e) {
			log.error(e.toString());
			this.setLastExecutionStatus(BicompConstants.FAILURE);
			log.debug("Updating last execution status to [FAILURE]");
			
			this.buildErrorNotification(this.getClass().getName(), this.getClass().getName(), NotificationLevelEnum.ERROR.name(), e);
		}
	}

}
