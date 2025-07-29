package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.TipsPaymentExtractorBean;


@DisallowConcurrentExecution
public class TipsPaymentExtractorJob extends QuartzJobBean {
	
	@Autowired
	private TipsPaymentExtractorBean tipsPaymentExtractorBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.tipsPaymentExtractorBean.preExecute(context);
		try {
			this.tipsPaymentExtractorBean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.tipsPaymentExtractorBean.postExecute(context);
	}

}
