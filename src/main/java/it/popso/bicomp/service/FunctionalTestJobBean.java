package it.popso.bicomp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@Scope("prototype")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class FunctionalTestJobBean extends ReportProcessor {
	
	public FunctionalTestJobBean(final TimerService timerService, final NotificationService notificationService) {
		super(timerService, notificationService);
	}


	@Override
	public void execute() {
		
		log.info("START functional job");
		
		this.setLastExecutionStatus("SUCCESSFULL");
		log.info("END functional job");
	}

}
