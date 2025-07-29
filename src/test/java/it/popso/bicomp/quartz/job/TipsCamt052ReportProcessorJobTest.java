package it.popso.bicomp.quartz.job;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.service.CgsCamt053ReportProcessorBean;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TipsCamt052ReportProcessorJobTest {
	
	@Mock
	private CgsCamt053ReportProcessorBean bean;
	
	@Mock
	private JobExecutionContext jobExecutionContext;
	
	@InjectMocks
	private CgsCamt053ReportProcessorJob job;
	
	@Test
	@Order(1)
	void testExecuteInternal() throws JobExecutionException, IOException  {
		
		Mockito.doNothing().when(this.bean).preExecute(this.jobExecutionContext);
		Mockito.doNothing().when(this.bean).execute();
		Mockito.doNothing().when(this.bean).postExecute(this.jobExecutionContext);
		
		this.job.executeInternal(jobExecutionContext);
		
		verify(this.bean, times(1)).preExecute(this.jobExecutionContext);
		verify(this.bean, times(1)).execute();
		verify(this.bean, times(1)).postExecute(this.jobExecutionContext);
	}
	
	@Test
	@Order(2)
	void testExecuteInternalException() throws JobExecutionException, IOException  {
		
		Mockito.doNothing().when(this.bean).preExecute(this.jobExecutionContext);
		Mockito.doThrow(IOException.class).when(this.bean).execute();
		Mockito.doNothing().when(this.bean).postExecute(this.jobExecutionContext);
		
		assertThrows(JobExecutionException.class, () -> {
			this.job.executeInternal(jobExecutionContext);
		});
		
		verify(this.bean, times(1)).preExecute(this.jobExecutionContext);
		verify(this.bean, times(1)).execute();
		verify(this.bean, times(0)).postExecute(this.jobExecutionContext);
	}

}
