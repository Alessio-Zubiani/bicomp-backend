package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.model.TimerStatusEnum;
import it.popso.bicomp.quartz.config.JobScheduleCreator;
import it.popso.bicomp.service.impl.SchedulerServiceImpl;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class SchedulerServiceImplTest {
	
	@Mock
	private SchedulerFactoryBean schedulerFactoryBean;
	
	@Mock
	private Scheduler scheduler;
	
	@Mock
	private JobDetail jobDetail;
	
	@Mock
	private CronTrigger cronTrigger;
	
	@Mock
	private JobScheduleCreator scheduleCreator;
	
	@Mock
	private ApplicationContext context;
	
	private SchedulerServiceImpl service;
	
	
	@BeforeEach
    public void setup() {
		this.service = new SchedulerServiceImpl(this.schedulerFactoryBean, this.context, this.scheduleCreator);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(1)
	void testEnableNotExists() throws SchedulerException, ParseException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.checkExists(Mockito.any(JobKey.class))).thenReturn(false);
		Mockito.when(this.scheduleCreator.createJob(Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString())).thenReturn(this.jobDetail);
		Mockito.when(this.scheduleCreator.createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt())).thenReturn(this.cronTrigger);
		Mockito.when(this.scheduler.scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class))).thenReturn(new Date());
		
		this.service.enable(t);
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).checkExists(Mockito.any(JobKey.class));
		verify(this.scheduleCreator, times(1)).createJob(
				Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString());
		verify(this.scheduleCreator, times(1)).createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt());
		verify(this.scheduler, times(1)).scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(2)
	void testEnableAlreadyExists() throws SchedulerException, ParseException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.checkExists(Mockito.any(JobKey.class))).thenReturn(true);
		
		assertThrows(BicompException.class, () -> {
			this.service.enable(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).checkExists(Mockito.any(JobKey.class));
		verify(this.scheduleCreator, times(0)).createJob(
				Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString());
		verify(this.scheduleCreator, times(0)).createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt());
		verify(this.scheduler, times(0)).scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(3)
	void testEnableSchedulerException() throws SchedulerException, ParseException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.checkExists(Mockito.any(JobKey.class))).thenReturn(false);
		Mockito.when(this.scheduleCreator.createJob(Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString())).thenReturn(this.jobDetail);
		Mockito.when(this.scheduleCreator.createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt())).thenReturn(this.cronTrigger);
		Mockito.when(this.scheduler.scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class))).thenThrow(new SchedulerException("Scheduler error"));
		
		assertThrows(BicompException.class, () -> {
			this.service.enable(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).checkExists(Mockito.any(JobKey.class));
		verify(this.scheduleCreator, times(1)).createJob(
				Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString());
		verify(this.scheduleCreator, times(1)).createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt());
		verify(this.scheduler, times(1)).scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(4)
	void testEnableClassNotFoundException() throws SchedulerException, ParseException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("test_job_class")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		BDDMockito.given(this.scheduleCreator.createJob(Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString()))
			.willAnswer(invocation -> {
				throw new ClassNotFoundException(new StringBuilder("Not found class: [").append(t.getJobClass()).append("]").toString());
			});
		
		assertThrows(BicompException.class, () -> {
			this.service.enable(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(0)).checkExists(Mockito.any(JobKey.class));
		verify(this.scheduleCreator, times(0)).createJob(
				Mockito.any(Class.class), Mockito.anyBoolean(), Mockito.any(ApplicationContext.class), 
				Mockito.anyString(), Mockito.anyString());
		verify(this.scheduleCreator, times(0)).createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt());
		verify(this.scheduler, times(0)).scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class));
	}
	
	@Test
	@Order(5)
	void testDisable() throws SchedulerException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.deleteJob(Mockito.any(JobKey.class))).thenReturn(true);
		
		this.service.disable(t);
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).deleteJob(Mockito.any(JobKey.class));
	}
	
	@Test
	@Order(6)
	void testDisableException() throws SchedulerException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.deleteJob(Mockito.any(JobKey.class))).thenThrow(new SchedulerException("Scheduler error"));
		
		assertThrows(BicompException.class, () -> {
			this.service.disable(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).deleteJob(Mockito.any(JobKey.class));
	}
	
	@Test
	@Order(7)
	void testUpdateScheduling() throws SchedulerException, ParseException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduleCreator.createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt())).thenReturn(this.cronTrigger);
		Mockito.when(this.scheduler.rescheduleJob(Mockito.any(TriggerKey.class), Mockito.any(Trigger.class)))
				.thenReturn(new Date());
		
		this.service.updateScheduling(t);
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduleCreator, times(1)).createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt());
		verify(this.scheduler, times(1)).rescheduleJob(Mockito.any(TriggerKey.class), Mockito.any(Trigger.class));
	}
	
	@Test
	@Order(8)
	void testUpdateSchedulingException() throws SchedulerException, ParseException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduleCreator.createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt())).thenReturn(this.cronTrigger);
		Mockito.when(this.scheduler.rescheduleJob(Mockito.any(TriggerKey.class), Mockito.any(Trigger.class)))
				.thenThrow(new SchedulerException("Scheduler error"));
		
		assertThrows(BicompException.class, () -> {
			this.service.updateScheduling(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduleCreator, times(1)).createCronTrigger(Mockito.anyString(), Mockito.any(Date.class), 
				Mockito.anyString(), Mockito.anyInt());
		verify(this.scheduler, times(1)).rescheduleJob(Mockito.any(TriggerKey.class), Mockito.any(Trigger.class));
	}
	
	@Test
	@Order(9)
	void testRunNow() throws SchedulerException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.doNothing().when(this.scheduler).triggerJob(Mockito.any(JobKey.class));
		
		this.service.runNow(t);
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).triggerJob(Mockito.any(JobKey.class));
	}
	
	@Test
	@Order(10)
	void testRunNowException() throws SchedulerException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.doThrow(new SchedulerException("Scheduler error")).when(this.scheduler).triggerJob(Mockito.any(JobKey.class));
		
		assertThrows(BicompException.class, () -> {
			this.service.runNow(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).triggerJob(Mockito.any(JobKey.class));
	}
	
	@Test
	@Order(11)
	void testUnscheduleAndDelete() throws SchedulerException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.deleteJob(Mockito.any(JobKey.class))).thenReturn(true);
		
		this.service.unscheduleAndDelete(t);
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).deleteJob(Mockito.any(JobKey.class));
	}
	
	@Test
	@Order(12)
	void testUnscheduleAndDeleteException() throws SchedulerException {
		
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.TipsCamt052ReportProcessorJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.schedulerFactoryBean.getScheduler()).thenReturn(this.scheduler);
		Mockito.when(this.scheduler.deleteJob(Mockito.any(JobKey.class))).thenThrow(new SchedulerException("Scheduler error"));
		
		assertThrows(BicompException.class, () -> {
			this.service.unscheduleAndDelete(t);
		});
		
		verify(this.schedulerFactoryBean, times(1)).getScheduler();
		verify(this.scheduler, times(1)).deleteJob(Mockito.any(JobKey.class));
	}
	
	@Test
	@Order(13)
	void testSchedulerServiceImplEquals() {
		
		SchedulerServiceImpl test = new SchedulerServiceImpl(this.schedulerFactoryBean, this.context, this.scheduleCreator);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(14)
	void testSchedulerServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(15)
	void testSchedulerServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(16)
	void testSchedulerServiceImplEqualsNull() {
		
		this.service = new SchedulerServiceImpl(null, null, null);
		SchedulerServiceImpl test = new SchedulerServiceImpl(null, null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(17)
	void testSchedulerServiceImplNotNull() {
		
		this.service = new SchedulerServiceImpl(null, null, null);
		SchedulerServiceImpl test = new SchedulerServiceImpl(this.schedulerFactoryBean, this.context, this.scheduleCreator);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(18)
	void testSchedulerServiceImplNotEqualsFactoryNull() {
		
		SchedulerServiceImpl test = new SchedulerServiceImpl(null, this.context, this.scheduleCreator);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(19)
	void testSchedulerServiceImplNotEqualsContextNull() {
		
		SchedulerServiceImpl test = new SchedulerServiceImpl(this.schedulerFactoryBean, null, this.scheduleCreator);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(20)
	void testSchedulerServiceImplNotEqualsCreatorNull() {
		
		SchedulerServiceImpl test = new SchedulerServiceImpl(this.schedulerFactoryBean, this.context, null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(21)
	void testSchedulerServiceImplEqualsHashCode() {
		
		SchedulerServiceImpl test = new SchedulerServiceImpl(this.schedulerFactoryBean, this.context, this.scheduleCreator);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(22)
	void testSchedulerServiceImplNotEqualsHashCode() {
		
		SchedulerServiceImpl test = new SchedulerServiceImpl(null, null, null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}

}
