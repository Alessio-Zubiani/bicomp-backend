package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.TipsCamt053ReportProcessorBean;


@DisallowConcurrentExecution
public class TipsCamt053ReportProcessorJob extends QuartzJobBean {
	
	@Autowired
	private TipsCamt053ReportProcessorBean tipsCamt053Bean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.tipsCamt053Bean.preExecute(context);
		try {
			this.tipsCamt053Bean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.tipsCamt053Bean.postExecute(context);
	}

}
