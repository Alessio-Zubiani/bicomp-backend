package it.popso.bicomp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@Scope("prototype")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class DeleteNotificationsBean extends ReportProcessor {
	
	public DeleteNotificationsBean(final TimerService timerService, final NotificationService notificationService) {
		super(timerService, notificationService);
	}


	@Override
	public void execute() {
		
		log.info("START deleting old notifications");
		this.deleteOldNotifications();
		
		this.setLastExecutionStatus("SUCCESSFULL");
		log.info("END deleting old notifications");
	}

}
