package it.popso.bicomp.quartz.job;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import it.popso.bicomp.service.TipsCamt052ReportProcessorBean;


@DisallowConcurrentExecution
public class TipsCamt052ReportProcessorJob extends QuartzJobBean {
	
	@Autowired
	private TipsCamt052ReportProcessorBean tipsCamt052Bean;

	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		this.tipsCamt052Bean.preExecute(context);
		try {
			this.tipsCamt052Bean.execute();
		} 
		catch (IOException e) {
			throw new JobExecutionException(e);
		}
		this.tipsCamt052Bean.postExecute(context);
	}

}
