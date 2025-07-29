package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.Rt1RsfReportProcessorBean;


@DisallowConcurrentExecution
public class Rt1RsfReportProcessorJob extends QuartzJobBean {
	
	@Autowired
	private Rt1RsfReportProcessorBean rt1RsfBean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.rt1RsfBean.preExecute(context);
		try {
			this.rt1RsfBean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.rt1RsfBean.postExecute(context);
	}

}
