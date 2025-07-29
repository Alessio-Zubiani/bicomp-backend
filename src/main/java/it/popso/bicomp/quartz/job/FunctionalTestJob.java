package it.popso.bicomp.quartz.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.FunctionalTestJobBean;


@DisallowConcurrentExecution
public class FunctionalTestJob extends QuartzJobBean {
	
	@Autowired
	private FunctionalTestJobBean functionalTestJobBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.functionalTestJobBean.preExecute(context);
		this.functionalTestJobBean.execute();
		this.functionalTestJobBean.postExecute(context);
	}

}
