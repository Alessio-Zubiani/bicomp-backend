package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.instancio.Select.field;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.instancio.Instancio;
import org.instancio.Model;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.TimerDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.model.TimerStatusEnum;
import it.popso.bicomp.repository.TimerRepository;
import it.popso.bicomp.service.impl.TimerServiceImpl;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TimerServiceImplTest {
	
	@Mock
	private TimerRepository repository;
	
	@Mock
	private SchedulerService schedulerService;
	
	private TimerServiceImpl service;
	
	
	@BeforeEach
    public void setup() {
		this.service = new TimerServiceImpl(this.repository, this.schedulerService);
	}
	
	@Test
	@Order(1)
	void testFindAllTimer() {
		
		List<Timer> expectedTimers = Instancio.ofList(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob")).size(3).create();
		Mockito.when(this.repository.findAllByOrderByJobNameAsc()).thenReturn(expectedTimers);
		
		List<TimerDto> timers = this.service.findAllTimer();
		
		assertThat(timers).hasSize(3);
		assertThat(timers.get(0).getJobName()).isEqualTo(expectedTimers.get(0).getJobName());
		
		verify(this.repository, times(1)).findAllByOrderByJobNameAsc();
	}
	
	@Test
	@Order(2)
	void testFindAllTimerNotFound() {
				
		List<Timer> expectedTimers = Arrays.asList();
		Mockito.when(this.repository.findAllByOrderByJobNameAsc()).thenReturn(expectedTimers);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findAllTimer();
		});
		
		verify(this.repository, times(1)).findAllByOrderByJobNameAsc();
	}
	
	@Test
	@Order(3)
	void testCreateTimerEnabled() {
		
		TimerDto tDto = Instancio.create(createTimerDtoModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		
		Mockito.when(this.repository.findByJobName(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.findByJobClass(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		Mockito.doNothing().when(this.schedulerService).enable(Mockito.any(Timer.class));
		
		this.service.createTimer(tDto);
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
		verify(this.repository, times(1)).findByJobClass(Mockito.anyString());
		verify(this.schedulerService, times(1)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(2)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(4)
	void testEnableTimer() {
		
		Timer t = Instancio.create(createTimerModel(false, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.doNothing().when(this.schedulerService).enable(Mockito.any(Timer.class));
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.enableTimer(t.getJobId());
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(1)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(1)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(5)
	void testEnableTimerNotFound() {
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.enableTimer(BigDecimal.ONE);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(0)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(6)
	void testDisableTimer() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.doNothing().when(this.schedulerService).disable(Mockito.any(Timer.class));
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.disableTimer(t.getJobId());
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(1)).disable(Mockito.any(Timer.class));
		verify(this.repository, times(1)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(7)
	void testDisableTimerNotFound() {
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.disableTimer(BigDecimal.ONE);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(0)).disable(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(8)
	void testUpdateTimerSchedulingDisabled() {
		
		TimerDto tDto = Instancio.create(createTimerDtoModel(false, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Timer t = Instancio.create(createTimerModel(false, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.updateTimerScheduling(tDto);
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.repository, times(1)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(9)
	void testUpdateTimerSchedulingEnabled() {
		
		TimerDto tDto = Instancio.create(createTimerDtoModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		
		assertThrows(BicompException.class, () -> {
			this.service.updateTimerScheduling(tDto);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(10)
	void testUpdateTimerSchedulingNotFound() {
		
		TimerDto tDto = Instancio.create(createTimerDtoModel(false, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.updateTimerScheduling(tDto);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(0)).unscheduleAndDelete(Mockito.any(Timer.class));
		verify(this.schedulerService, times(0)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(11)
	void testExecuteTimer() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.doNothing().when(this.schedulerService).runNow(Mockito.any(Timer.class));
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.executeTimer(BigDecimal.ONE);
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(1)).runNow(Mockito.any(Timer.class));
		verify(this.repository, times(1)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(12)
	void testExecuteTimerNotFound() {
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.executeTimer(BigDecimal.ONE);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(0)).runNow(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(13)
	void testUnscheduleAndDeleteTimerEnabled() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.doNothing().when(this.schedulerService).unscheduleAndDelete(Mockito.any(Timer.class));
		Mockito.doNothing().when(this.repository).deleteById(Mockito.any(BigDecimal.class));
		
		this.service.unscheduleAndDeleteTimer(t.getJobId());
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(1)).unscheduleAndDelete(Mockito.any(Timer.class));
		verify(this.repository, times(1)).delete(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(14)
	void testUnscheduleAndDeleteTimerDisabled() {
		
		Timer t = Instancio.create(createTimerModel(false, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.doNothing().when(this.repository).deleteById(Mockito.any(BigDecimal.class));
		
		this.service.unscheduleAndDeleteTimer(t.getJobId());
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(0)).unscheduleAndDelete(Mockito.any(Timer.class));
		verify(this.repository, times(1)).delete(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(15)
	void testUnscheduleAndDeleteTimerNotFound() {
		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.unscheduleAndDeleteTimer(BigDecimal.ONE);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.schedulerService, times(0)).unscheduleAndDelete(Mockito.any(Timer.class));
		verify(this.repository, times(0)).delete(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(16)
	void testFindByJobName() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Mockito.when(this.repository.findByJobName(Mockito.any(String.class))).thenReturn(Optional.of(t));
		
		Timer findByJobName = this.service.findByJobName("test_job_name_1");
		
		assertThat(findByJobName).isNotNull();
		assertThat(findByJobName.getJobName()).isEqualTo(t.getJobName());
		assertThat(findByJobName.getJobClass()).isEqualTo(t.getJobClass());
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
	}
	
	@Test
	@Order(17)
	void testFindByJobNameNotFound() {
		
		Mockito.when(this.repository.findByJobName(Mockito.any(String.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findByJobName(new String("test_job_name_1"));
		});
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
	}
	
	@Test
	@Order(18)
	void testUpdateTimer() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(t));
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.updateTimer(t);
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.repository, times(1)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(19)
	void testUpdateTimerNotFound() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.updateTimer(t);
		});
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(20)
	void testCreateTimerDisabled() {
		
		TimerDto tDto = Instancio.create(createTimerDtoModel(false, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		Timer t = Instancio.create(createTimerModel(false, null));
		Mockito.when(this.repository.findByJobName(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.findByJobClass(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.createTimer(tDto);
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
		verify(this.schedulerService, times(0)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(1)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(21)
	void testCreateTimerClassNotFoundException() {
		
		TimerDto tDto = Instancio.create(createTimerDtoModel(false, "test_job_class"));
		Mockito.when(this.repository.findByJobName(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.findByJobClass(Mockito.anyString())).thenReturn(Optional.empty());
		
		assertThrows(BicompException.class, () -> {
			this.service.createTimer(tDto);
		});
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
		verify(this.repository, times(1)).findByJobClass(Mockito.anyString());
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
		verify(this.schedulerService, times(0)).enable(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(22)
	void testScheduleTimer() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));
		List<Timer> enabledTimers = Arrays.asList(t);
		Mockito.when(this.repository.findEnabledTimer()).thenReturn(enabledTimers);
		Mockito.doNothing().when(this.schedulerService).enable(Mockito.any(Timer.class));
		Mockito.when(this.repository.save(Mockito.any(Timer.class))).thenReturn(t);
		
		this.service.scheduleTimer();
		
		verify(this.repository, times(1)).findEnabledTimer();
		verify(this.schedulerService, times(enabledTimers.size())).enable(Mockito.any(Timer.class));
		verify(this.repository, times(enabledTimers.size())).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(23)
	void testScheduleTimerEmpty() {		
		List<Timer> enabledTimers = Arrays.asList();
		Mockito.when(this.repository.findEnabledTimer()).thenReturn(enabledTimers);
		
		this.service.scheduleTimer();
		
		verify(this.repository, times(1)).findEnabledTimer();
		verify(this.schedulerService, times(enabledTimers.size())).enable(Mockito.any(Timer.class));
		verify(this.repository, times(enabledTimers.size())).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(24)
	void testScheduleTimerException() {
		
		Timer t = Instancio.create(createTimerModel(true, "it.popso.bicomp.quartz.job.FunctionalTestJob"));		
		List<Timer> enabledTimers = Arrays.asList(t);
		Mockito.when(this.repository.findEnabledTimer()).thenReturn(enabledTimers);
		Mockito.doThrow(new BicompException("BicompException")).when(this.schedulerService).enable(Mockito.any(Timer.class));
		
		assertThrows(BicompException.class, () -> {
			this.service.scheduleTimer();
		});
		
		verify(this.repository, times(1)).findEnabledTimer();
		verify(this.schedulerService, times(1)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(25)
	void testTimerServiceImplEquals() {
		
		TimerServiceImpl test = new TimerServiceImpl(this.repository, this.schedulerService);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(26)
	void testTimerServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(27)
	void testTimerServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(28)
	void testTimerServiceImplEqualsNull() {
		
		this.service = new TimerServiceImpl(null, null);
		TimerServiceImpl test = new TimerServiceImpl(null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(29)
	void testTimerServiceImplNotNull() {
		
		this.service = new TimerServiceImpl(null, null);
		TimerServiceImpl test = new TimerServiceImpl(this.repository, this.schedulerService);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(30)
	void testTimerServiceImplNotEqualsRepositoryNull() {
		
		TimerServiceImpl test = new TimerServiceImpl(null, this.schedulerService);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(31)
	void testTimerServiceImplNotEqualsSchedulerNull() {
		
		TimerServiceImpl test = new TimerServiceImpl(this.repository, null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(32)
	void testTimerServiceImplEqualsHashCode() {
		
		TimerServiceImpl test = new TimerServiceImpl(this.repository, this.schedulerService);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(33)
	void testTimerServiceImplNotEqualsHashCode() {
		
		TimerServiceImpl test = new TimerServiceImpl(null, null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}
	
	@Test
	@Order(34)
	void testCreateTimerSameJobName() {
		TimerDto tDto = TimerDto.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.FunctionalTestJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.FunctionalTestJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.repository.findByJobName(Mockito.anyString())).thenReturn(Optional.of(t));
		
		assertThrows(BicompException.class, () -> {
			this.service.createTimer(tDto);
		});
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
		verify(this.repository, times(0)).findByJobClass(Mockito.anyString());
		verify(this.schedulerService, times(0)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	@Test
	@Order(35)
	void testCreateTimerSameJobClass() {
		TimerDto tDto = TimerDto.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.FunctionalTestJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Timer t = Timer.builder()
				.jobId(new BigDecimal(1))
				.jobName("test_job_name_1")
				.jobGroup("test_job_group")
				.jobStatus(TimerStatusEnum.SCHEDULED)
				.jobClass("it.popso.bicomp.quartz.job.FunctionalTestJob")
				.cronExpression("0 0 16 * * ?")
				.jobDescription("Job for testing repository")
				.cronJob('Y')
				.enabled('Y')
				.build();
		Mockito.when(this.repository.findByJobName(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.findByJobClass(Mockito.anyString())).thenReturn(Optional.of(t));
		
		assertThrows(BicompException.class, () -> {
			this.service.createTimer(tDto);
		});
		
		verify(this.repository, times(1)).findByJobName(Mockito.anyString());
		verify(this.repository, times(1)).findByJobClass(Mockito.anyString());
		verify(this.schedulerService, times(0)).enable(Mockito.any(Timer.class));
		verify(this.repository, times(0)).save(Mockito.any(Timer.class));
	}
	
	public static Model<Timer> createTimerModel(boolean enabled, String jobClass) {
		
		Model<Timer> model = Instancio.of(Timer.class)
				.ignore(field(Timer::getInterfaceName))
				.generate(field(Timer::getJobId), gen -> gen.math().bigDecimal().scale(0))
				.generate(field(Timer::getJobName), gen -> gen.string().prefix("test_").suffix("_job"))
				.set(field(Timer::getJobDescription), "Job description")
				.set(field(Timer::getJobGroup), "GROUP_1")
				.set(field(Timer::getJobClass), jobClass)
				.generate(field(Timer::getJobStatus), gen -> gen.oneOf(TimerStatusEnum.SCHEDULED, TimerStatusEnum.STOPPED, TimerStatusEnum.EDITED))
				.generate(field(Timer::getCronExpression), gen -> gen.oneOf("0 0 10 ? * MON-FRI", "0 0/10 * * * ?"))
				.generate(field(Timer::getCronJob), gen -> gen.oneOf('Y', 'N'))
				.generate(field(Timer::getEnabled), gen -> enabled ? gen.oneOf('Y') : gen.oneOf('N'))
				.generate(field(Timer::getLastExecutionStatus), gen -> gen.oneOf("SUCCESSFULL", "FAILURE"))
				.toModel();
		
		return model;
	}
	
	public static Model<TimerDto> createTimerDtoModel(boolean enabled, String jobClass) {
		
		Model<TimerDto> model = Instancio.of(TimerDto.class)
				.ignore(field(TimerDto::getInterfaceName))
				.generate(field(TimerDto::getJobId), gen -> gen.math().bigDecimal().scale(0))
				.generate(field(TimerDto::getJobName), gen -> gen.string().prefix("test_").suffix("job"))
				.set(field(TimerDto::getJobDescription), "Job description")
				.set(field(TimerDto::getJobGroup), "GROUP_1")
				.set(field(TimerDto::getJobClass), jobClass)
				.generate(field(TimerDto::getJobStatus), gen -> gen.oneOf(TimerStatusEnum.SCHEDULED, TimerStatusEnum.STOPPED, TimerStatusEnum.EDITED))
				.generate(field(TimerDto::getCronExpression), gen -> gen.oneOf("0 0 10 ? * MON-FRI", "0 0/10 * * * ?"))
				.generate(field(TimerDto::getCronJob), gen -> gen.oneOf('Y', 'N'))
				.generate(field(TimerDto::getEnabled), gen -> enabled ? gen.oneOf('Y') : gen.oneOf('N'))
				.generate(field(TimerDto::getLastExecutionStatus), gen -> gen.oneOf("SUCCESSFULL", "FAILURE"))
				.toModel();
		
		return model;
	}

}
