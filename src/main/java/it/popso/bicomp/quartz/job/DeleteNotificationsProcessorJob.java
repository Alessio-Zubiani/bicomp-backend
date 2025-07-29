package it.popso.bicomp.quartz.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.DeleteNotificationsBean;


@DisallowConcurrentExecution
public class DeleteNotificationsProcessorJob extends QuartzJobBean {
	
	@Autowired
	private DeleteNotificationsBean deleteNotificationsBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.deleteNotificationsBean.preExecute(context);
		this.deleteNotificationsBean.execute();
		this.deleteNotificationsBean.postExecute(context);
	}

}
