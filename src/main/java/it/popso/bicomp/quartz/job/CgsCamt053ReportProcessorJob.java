package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.CgsCamt053ReportProcessorBean;


@DisallowConcurrentExecution
public class CgsCamt053ReportProcessorJob extends QuartzJobBean {
	
	@Autowired
	private CgsCamt053ReportProcessorBean cgsBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.cgsBean.preExecute(context);
		try {
			this.cgsBean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.cgsBean.postExecute(context);
	}

}
