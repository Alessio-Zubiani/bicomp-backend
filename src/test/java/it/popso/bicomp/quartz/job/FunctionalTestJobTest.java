package it.popso.bicomp.quartz.job;

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

import it.popso.bicomp.service.FunctionalTestJobBean;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class FunctionalTestJobTest {
	
	@Mock
	private FunctionalTestJobBean bean;
	
	@Mock
	private JobExecutionContext jobExecutionContext;
	
	@InjectMocks
	private FunctionalTestJob job;
	
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

}
