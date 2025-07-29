package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.Rt1PsrReportProcessorBean;


@DisallowConcurrentExecution
public class Rt1PsrReportProcessorJob extends QuartzJobBean {
	
	@Autowired
	private Rt1PsrReportProcessorBean rt1PsrBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.rt1PsrBean.preExecute(context);
		try {
			this.rt1PsrBean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.rt1PsrBean.postExecute(context);
	}

}
