package it.popso.bicomp.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.TimerDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.model.TimerStatusEnum;
import it.popso.bicomp.service.TimerService;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TimerControllerTest {
	
	@Mock
	private TimerService timerService;
	
	private TimerController controller;
	

	@BeforeEach
	public void setup() {
		this.controller = new TimerController(this.timerService);
	}
	
	@Test
	@Order(1)
	void testGetTimer() {
		
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
		Mockito.when(this.timerService.findById(Mockito.any(BigDecimal.class))).thenReturn(t);
		
		TimerDto result = (TimerDto) this.controller.getTimer(BigDecimal.ONE).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getJobName()).isEqualTo(t.getJobName());
		
		verify(this.timerService, times(1)).findById(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(2)
	void testGetTimerNotFound() {
		
		Mockito.when(this.timerService.findById(Mockito.any(BigDecimal.class))).thenThrow(new ResourceNotFoundException("ResourceNotFoundException"));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getTimer(BigDecimal.ONE);
		});
		
		verify(this.timerService, times(1)).findById(Mockito.any(BigDecimal.class));
	}

	@Test
	@Order(3)
	void testGetAllTimers() {
		
		List<TimerDto> list = Arrays.asList(TimerDto.builder()
			.jobId(new BigDecimal(1)) 
			.jobName("test_job_name_1")
			.jobGroup("test_job_group") 
			.jobStatus(TimerStatusEnum.SCHEDULED)
			.jobClass("test_job_class_1") 
			.cronExpression("0 0 16 * * ?")
			.jobDescription("Job for testing repository")
			.cronJob('Y') 
			.enabled('Y') 
			.build(), TimerDto.builder() 
			.jobId(new BigDecimal(2)) 
			.jobName("test_job_name_2") 
			.jobGroup("test_job_group")
			.jobStatus(TimerStatusEnum.STOPPED) 
			.jobClass("test_job_class_2")
			.cronExpression("0 0 16 * * ?") 
			.jobDescription("Job for testing repository")
			.cronJob('Y') 
			.enabled('N') 
			.build(), TimerDto.builder() 
			.jobId(new BigDecimal(3)) 
			.jobName("test_job_name_3")
			.jobGroup("test_job_group") 
			.jobStatus(TimerStatusEnum.SCHEDULED)
			.jobClass("test_job_class_3") 
			.cronExpression("0 0 16 * * ?")
			.jobDescription("Job for testing repository")
			.cronJob('Y') 
			.enabled('Y') 
			.build());
		Mockito.when(this.timerService.findAllTimer()).thenReturn(list);
		
		List<TimerDto> result = (List<TimerDto>) this.controller.getAllTimers().getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(1).getJobName()).isEqualTo(list.get(1).getJobName());
		
		verify(this.timerService, times(1)).findAllTimer();
	}
	
	@Test
	@Order(4)
	void testGetAllTimersNotFound() {
		
		Mockito.when(this.timerService.findAllTimer()).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getAllTimers();
		});
		
		verify(this.timerService, times(1)).findAllTimer();
	}
	
	@Test
	@Order(5)
	void testCreateTimer() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doNothing().when(this.timerService).createTimer(Mockito.any(TimerDto.class));
		
		String result = (String) this.controller.createTimer(t).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(t.getJobName());
		
		verify(this.timerService, times(1)).createTimer(Mockito.any(TimerDto.class));
	}
	
	@Test
	@Order(6)
	void testCreateTimerAlreadyExists() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(DataIntegrityViolationException.class).when(this.timerService).createTimer(Mockito.any(TimerDto.class));
		
		assertThrows(BicompException.class, () -> {
			this.controller.createTimer(t);
		});
		
		verify(this.timerService, times(1)).createTimer(Mockito.any(TimerDto.class));
	}
	
	@Test
	@Order(7)
	void testEnableTimer() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doNothing().when(this.timerService).enableTimer(Mockito.any(BigDecimal.class));
		
		BigDecimal result = (BigDecimal) this.controller.enableTimer(t.getJobId()).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(t.getJobId());
		
		verify(this.timerService, times(1)).enableTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(8)
	void testEnableTimerBicompException() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(BicompException.class).when(this.timerService).enableTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(BicompException.class, () -> {
			this.controller.enableTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).enableTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(9)
	void testEnableTimerNotFound() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(ResourceNotFoundException.class).when(this.timerService).enableTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.enableTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).enableTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(10)
	void testDisableTimer() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doNothing().when(this.timerService).disableTimer(Mockito.any(BigDecimal.class));
		
		BigDecimal result = (BigDecimal) this.controller.disableTimer(t.getJobId()).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(t.getJobId());
		
		verify(this.timerService, times(1)).disableTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(11)
	void testDisableTimerBicompException() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(BicompException.class).when(this.timerService).disableTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(BicompException.class, () -> {
			this.controller.disableTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).disableTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(12)
	void testDisableTimerNotFound() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(ResourceNotFoundException.class).when(this.timerService).disableTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.disableTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).disableTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(13)
	void testUpdateTimer() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doNothing().when(this.timerService).updateTimerScheduling(Mockito.any(TimerDto.class));
		
		BigDecimal result = (BigDecimal) this.controller.updateTimer(t).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(t.getJobId());
		
		verify(this.timerService, times(1)).updateTimerScheduling(Mockito.any(TimerDto.class));
	}
	
	@Test
	@Order(14)
	void testUpdateTimerBicompException() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(BicompException.class).when(this.timerService).updateTimerScheduling(Mockito.any(TimerDto.class));
		
		assertThrows(BicompException.class, () -> {
			this.controller.updateTimer(t);
		});
		
		verify(this.timerService, times(1)).updateTimerScheduling(Mockito.any(TimerDto.class));
	}
	
	@Test
	@Order(15)
	void testUpdateTimerNotFound() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(ResourceNotFoundException.class).when(this.timerService).updateTimerScheduling(Mockito.any(TimerDto.class));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.updateTimer(t);
		});
		
		verify(this.timerService, times(1)).updateTimerScheduling(Mockito.any(TimerDto.class));
	}
	
	@Test
	@Order(16)
	void testExecuteTimer() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doNothing().when(this.timerService).executeTimer(Mockito.any(BigDecimal.class));
		
		BigDecimal result = (BigDecimal) this.controller.executeTimer(t.getJobId()).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(t.getJobId());
		
		verify(this.timerService, times(1)).executeTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(17)
	void testExecuteTimerBicompException() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(BicompException.class).when(this.timerService).executeTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(BicompException.class, () -> {
			this.controller.executeTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).executeTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(18)
	void testExecuteTimerNotFound() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(ResourceNotFoundException.class).when(this.timerService).executeTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.executeTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).executeTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(19)
	void testDeleteTimer() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doNothing().when(this.timerService).unscheduleAndDeleteTimer(Mockito.any(BigDecimal.class));
		
		BigDecimal result = (BigDecimal) this.controller.deleteTimer(t.getJobId()).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(t.getJobId());
		
		verify(this.timerService, times(1)).unscheduleAndDeleteTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(20)
	void testDeleteTimerBicompException() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(BicompException.class).when(this.timerService).unscheduleAndDeleteTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(BicompException.class, () -> {
			this.controller.deleteTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).unscheduleAndDeleteTimer(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(21)
	void testDeleteTimerNotFound() {
		
		TimerDto t = TimerDto.builder()
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
		Mockito.doThrow(ResourceNotFoundException.class).when(this.timerService).unscheduleAndDeleteTimer(Mockito.any(BigDecimal.class));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.deleteTimer(t.getJobId());
		});
		
		verify(this.timerService, times(1)).unscheduleAndDeleteTimer(Mockito.any(BigDecimal.class));
	}

}
