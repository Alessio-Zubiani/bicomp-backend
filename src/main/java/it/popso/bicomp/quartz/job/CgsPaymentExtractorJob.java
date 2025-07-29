package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.CgsPaymentExtractorBean;


@DisallowConcurrentExecution
public class CgsPaymentExtractorJob extends QuartzJobBean {
	
	@Autowired
	private CgsPaymentExtractorBean cgsPaymentExtractorBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.cgsPaymentExtractorBean.preExecute(context);
		try {
			this.cgsPaymentExtractorBean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.cgsPaymentExtractorBean.postExecute(context);
	}

}
