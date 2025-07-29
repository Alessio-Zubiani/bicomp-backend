package it.popso.bicomp.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.messages.Item;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.model.TimerStatusEnum;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TipsCamt052ReportProcessorBeanTest {
	
	@Mock
	private TipsService tipsService;
	
	@Mock
	private TimerService timerService;
	
	@Mock
	private JobExecutionContext context;
	
	@Mock
	private NotificationService notificationService;
	
	private TipsCamt052ReportProcessorBean bean;
	
	
	@BeforeEach
    public void setup() {
		this.bean = new TipsCamt052ReportProcessorBean(this.tipsService, this.notificationService, this.timerService);
	}
	
	@Test
	@Order(1)
	void testExecuteListEmpty() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList());
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(0)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(2)
	void testExecute() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.052.xml")));
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		Mockito.doNothing().when(this.tipsService).processCamt052Report(Mockito.anyList());
		Mockito.doNothing().when(this.tipsService).moveToBackupFolder052(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(1)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(1)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(3)
	void testExecuteBicompException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.052.xml")));
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		Mockito.doThrow(new BicompException("BicompException error")).when(this.tipsService).processCamt052Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(1)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(4)
	void testExecuteDataIntegrityViolationException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.052.xml")));
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		Mockito.doThrow(new DataIntegrityViolationException("DataIntegrityViolationException error")).when(this.tipsService).processCamt052Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(1)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(5)
	void testExecuteJAXBException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.052.xml")));
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		Mockito.doThrow(new JAXBException("JAXBException error")).when(this.tipsService).processCamt052Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(1)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(6)
	void testExecuteIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.052.xml")));
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		Mockito.doThrow(new IOException("IOException error")).when(this.tipsService).processCamt052Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(1)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(7)
	void testExecuteMoveIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.052.xml")));
		Mockito.when(this.tipsService.getCamt052Report()).thenReturn(list);
		Mockito.doThrow(new IOException("IOException error")).when(this.tipsService).moveToBackupFolder052(Mockito.anyList());
				
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(1)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(1)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(8)
	void testExecuteGetCamt052ReportIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		Mockito.when(this.tipsService.getCamt052Report()).thenThrow(new IOException("IOException error"));
				
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt052Report();
		verify(this.tipsService, times(0)).processCamt052Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder052(Mockito.anyList());
	}
	
	@Test
	@Order(9)
	void testPreExecute() throws JobExecutionException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("test_job_class_1")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		JobDataMap dataMap = new JobDataMap();
		dataMap.put("jobName", t.getJobName());
		
		Mockito.when(this.context.getMergedJobDataMap()).thenReturn(dataMap);
		Mockito.when(this.timerService.findByJobName(Mockito.anyString())).thenReturn(t);
		Mockito.doNothing().when(this.timerService).updateTimer(Mockito.any(Timer.class));
		
		this.bean.preExecute(this.context);
		
		verify(this.context, times(1)).getMergedJobDataMap();
		verify(this.timerService, times(1)).findByJobName(Mockito.anyString());
		verify(this.timerService, times(1)).updateTimer(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(10)
	void testPreExecuteResourceNotFoundException() throws JobExecutionException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("test_job_class_1")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		JobDataMap dataMap = new JobDataMap();
		dataMap.put("jobName", t.getJobName());
		
		Mockito.when(this.context.getMergedJobDataMap()).thenReturn(dataMap);
		Mockito.when(this.timerService.findByJobName(Mockito.anyString())).thenThrow(new ResourceNotFoundException("ResourceNotFoundException error"));
		
		assertThrows(JobExecutionException.class, () -> {
			this.bean.preExecute(this.context);
		});
		
		verify(this.context, times(1)).getMergedJobDataMap();
		verify(this.timerService, times(1)).findByJobName(Mockito.anyString());
		verify(this.timerService, times(0)).updateTimer(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(11)
	void testPostExecute() throws JobExecutionException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("test_job_class_1")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		JobDataMap dataMap = new JobDataMap();
		dataMap.put("jobName", t.getJobName());
		
		Mockito.when(this.context.getMergedJobDataMap()).thenReturn(dataMap);
		Mockito.when(this.timerService.findByJobName(Mockito.anyString())).thenReturn(t);
		Mockito.doNothing().when(this.timerService).updateTimer(Mockito.any(Timer.class));
		
		this.bean.postExecute(this.context);
		
		verify(this.context, times(1)).getMergedJobDataMap();
		verify(this.timerService, times(1)).findByJobName(Mockito.anyString());
		verify(this.timerService, times(1)).updateTimer(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(12)
	void testPostExecuteResourceNotFoundException() throws JobExecutionException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("test_job_class_1")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		JobDataMap dataMap = new JobDataMap();
		dataMap.put("jobName", t.getJobName());
		
		Mockito.when(this.context.getMergedJobDataMap()).thenReturn(dataMap);
		Mockito.when(this.timerService.findByJobName(Mockito.anyString())).thenThrow(new ResourceNotFoundException("ResourceNotFoundException error"));
		
		assertThrows(JobExecutionException.class, () -> {
			this.bean.postExecute(this.context);
		});
		
		verify(this.context, times(1)).getMergedJobDataMap();
		verify(this.timerService, times(1)).findByJobName(Mockito.anyString());
		verify(this.timerService, times(0)).updateTimer(Mockito.any(Timer.class));
	}
	
	private Item mockItem(String objectName) {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.objectName()).thenReturn(objectName);
		
		return item;
	}

}
