package it.popso.bicomp.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.minio.messages.Item;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.model.TimerStatusEnum;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Setter
public abstract class ReportProcessor {
	
	private final TimerService timerService;
	private final NotificationService notificationService;
	private String lastExecutionStatus;

	protected ReportProcessor(TimerService timerService, NotificationService notificationService) {
		this.timerService = timerService;
		this.notificationService = notificationService;
	}
	
	
	public abstract void execute() throws IOException;
	
	public void preExecute(JobExecutionContext context) throws JobExecutionException {
		
		log.info("Starting job: [{}]", this.getClass().getSimpleName());
		
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		
		try {
			log.info("Searching timer with jobName: [{}]", jobDataMap.getString(BicompConstants.JOBNAME));
			Timer t = this.timerService.findByJobName(jobDataMap.getString(BicompConstants.JOBNAME));
			
			log.info("Updating job status to [RUNNING]");
			t.setLastStart(new Date());
			t.setJobStatus(TimerStatusEnum.RUNNING);
			this.timerService.updateTimer(t);
			log.info("Updated timer with jobName: [{}]", jobDataMap.getString(BicompConstants.JOBNAME));
		}
		catch(ResourceNotFoundException e) {
			throw new JobExecutionException(e);
		}
	}
	
	public void postExecute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		
		try {
			log.info("Searching timer with jobName: [{}]", jobDataMap.getString(BicompConstants.JOBNAME));
			Timer t = this.timerService.findByJobName(jobDataMap.getString(BicompConstants.JOBNAME));
			
			log.info("Updating job status to [SCHEDULED]");
			t.setLastExecutionStatus(this.lastExecutionStatus);
			t.setLastStop(new Date());
			t.setJobStatus(TimerStatusEnum.SCHEDULED);
			log.info("New timer's values: [{}]", t);
			
			this.timerService.updateTimer(t);
			log.info("Updated timer with jobName: [{}]", jobDataMap.getString(BicompConstants.JOBNAME));
			
			log.info("Ending job: [{}]", this.getClass().getSimpleName());
		}
		catch(ResourceNotFoundException e) {
			throw new JobExecutionException(e);
		}
	}
	
	public void buildErrorNotification(String title, String message, String livello, Exception e) {
		this.notificationService.createNotification(
			"BICOMP", title, StringUtils.stringUtils().customizeExceptionMessage(message, e), livello
		);
	}
	
	public void buildNotification(String title, String message, String livello) {
		this.notificationService.createNotification("BICOMP", title, message, livello);
	}
	
	public String listToString(List<Path> list) {
		return "[".concat(list.stream().map(Object::toString).collect(Collectors.joining(", "))).concat("]");
	}
	
	public String listItemToString(List<Item> list) {
		return "[".concat(list.stream().map(Item::objectName).collect(Collectors.joining(", "))).concat("]");
	}
	
	public void deleteOldNotifications() {
		this.notificationService.deleteNotifications();
	}

}
