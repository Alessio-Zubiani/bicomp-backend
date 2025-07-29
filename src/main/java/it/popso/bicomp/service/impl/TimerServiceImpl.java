package it.popso.bicomp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.popso.bicomp.dto.TimerDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.model.TimerStatusEnum;
import it.popso.bicomp.repository.TimerRepository;
import it.popso.bicomp.service.SchedulerService;
import it.popso.bicomp.service.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TimerServiceImpl implements TimerService {

	private final TimerRepository repository;
	private final SchedulerService schedulerService;

	
	@Override
	public List<TimerDto> findAllTimer() throws ResourceNotFoundException {
		
		List<Timer> timers = this.repository.findAllByOrderByJobNameAsc();
		if(timers.isEmpty()) {
			throw new ResourceNotFoundException("No timer found");
		}
		
		log.info("List all jobs: {}", Arrays.toString(timers.toArray()));
		List<TimerDto> dtos = new ArrayList<>();
		timers.forEach(t -> dtos.add(this.createDto(t)));
		
		return dtos;
	}
	
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { DataIntegrityViolationException.class, BicompException.class, ResourceNotFoundException.class }
	)
	@Override
	public void createTimer(TimerDto t) {
		
		// find by job name
		Optional<Timer> oTimer = this.repository.findByJobName(t.getJobName());
		if(oTimer.isPresent()) {
			throw new BicompException(new StringBuilder("Timer with Job Name [").append(t.getJobName()).append("] already exists").toString());
		}
		
		// find by job class
		oTimer = this.repository.findByJobClass(t.getJobClass());
		if(oTimer.isPresent()) {
			throw new BicompException(new StringBuilder("Timer with Job Class [").append(t.getJobClass()).append("] already exists").toString());
		}
		
		log.info("Creating timer with values: [{}]", t);
		try {
			// check if class exists
			Class.forName(t.getJobClass());
			
			Timer timer = this.repository.save(this.createEntity(t));
			if(timer.getEnabled().equals('Y')) {
				this.schedulerService.enable(timer);
				
				timer.setJobStatus(TimerStatusEnum.SCHEDULED);
				this.repository.save(timer);
				log.info("Saved & scheduled timer: [{}]", timer);
			}
			
			log.info("Saved timer: [{}]", timer);
		}
		catch(ClassNotFoundException e) {
			throw new BicompException(new StringBuilder("Class [").append(t.getJobClass()).append("] not found").toString());
		}
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { BicompException.class }
	)
	@Override
	public void scheduleTimer() throws BicompException {
		
		List<Timer> timers = this.repository.findEnabledTimer();
		if(timers.isEmpty()) {
			log.info("No timer to enable");
			return;
		}
		
		log.info("List enabled jobs: {}", Arrays.toString(timers.toArray()));		
		timers.forEach(t -> {
			this.schedulerService.enable(t);
			
			t.setJobStatus(TimerStatusEnum.SCHEDULED);
			this.repository.save(t);
		});
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { BicompException.class, ResourceNotFoundException.class }
	)
	@Override
	public void enableTimer(BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		log.info("Try enabling jobId: [{}]", id);
		
		Timer t = this.findById(id);
		
		this.schedulerService.enable(t);
		
		t.setJobStatus(TimerStatusEnum.SCHEDULED);
		t.setEnabled('Y');
		this.repository.save(t);
		log.info("JobName [{}] enabled", t.getJobName());
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { BicompException.class, ResourceNotFoundException.class }
	)
	@Override
	public void disableTimer(BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		log.info("Try disabling jobId: [{}]", id);
		
    	Timer t = this.findById(id);
    	
    	this.schedulerService.disable(t);
    	
    	t.setJobStatus(TimerStatusEnum.STOPPED);
     	t.setEnabled('N');
     	this.repository.save(t);
        log.info("JobName [{}] disabled", t.getJobName());
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { BicompException.class, ResourceNotFoundException.class }
	)
	@Override
	public void updateTimerScheduling(TimerDto t) throws BicompException, ResourceNotFoundException {

		log.info("Try to update scheduling of jobId: [{}]", t.getJobId());
		
		Timer timer = this.findById(t.getJobId());
		
		// se ENABLED=Y restituisco un'eccezione perchè non si possono modificare timer ABILITATI
		if(timer.getEnabled() == 'Y') {
			throw new BicompException(new StringBuilder("Cannot update ENABLED timer [").append(t.getJobName()).append("]").toString());
		}
		else {
			t.setJobStatus(TimerStatusEnum.EDITED);
			this.repository.save(this.createEntity(t));
		}
		
		log.info("JobName [{}] successfully updated", timer.getJobName());
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { BicompException.class, ResourceNotFoundException.class }
	)
	@Override
	public void executeTimer(BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		log.info("Try running now jobId: [{}]", id);
		
		Timer t = this.findById(id);
    	this.schedulerService.runNow(t);
        
        t.setJobStatus(TimerStatusEnum.STARTED);
        this.repository.save(t);
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { BicompException.class, ResourceNotFoundException.class }
	)
	@Override
	public void unscheduleAndDeleteTimer(BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		log.info("Try unscheduling and deleting jobId: [{}]", id);
		
		Timer t = this.findById(id);
		if(t.getEnabled() == 'Y') {
			this.schedulerService.unscheduleAndDelete(t);
		}
        
		log.info("Deleting jobId: [{}]", id);
		this.repository.delete(t);
	}

	@Override
	public Timer findByJobName(String jobName) throws ResourceNotFoundException {
		
		log.info("Searching timer with name: [{}]", jobName);
		Optional<Timer> o = this.repository.findByJobName(jobName);
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("Timer with JobName [").append(jobName).append("] not found").toString());
		}
		
		return o.get();
	}
	
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRED, 
		rollbackFor = { ResourceNotFoundException.class }
	)
	public void updateTimer(Timer t) throws ResourceNotFoundException {
		
		log.info("Updating timer with values: {}", t);
		Optional<Timer> timer = this.repository.findById(t.getJobId());
		if(!timer.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("Timer with JobName [").append(t.getJobName()).append("] not found").toString());
		}
		
		this.repository.save(t);
	}
	
	@Override
	public Timer findById(BigDecimal id) throws ResourceNotFoundException {
		
		log.info("Searching timer with ID: [{}]", id);
		Optional<Timer> o = this.repository.findById(id);
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("Timer with ID [").append(id).append("] not found").toString());
		}
		
		return o.get();
	}
	
	private TimerDto createDto(Timer t) {
		return TimerDto.builder()
				.jobId(t.getJobId())
				.jobName(t.getJobName())
				.jobGroup(t.getJobGroup())
				.jobStatus(t.getJobStatus())
				.lastExecutionStatus(t.getLastExecutionStatus())
				.jobClass(t.getJobClass())
				.cronExpression(t.getCronExpression())
				.jobDescription(t.getJobDescription())
				.interfaceName(t.getInterfaceName())
				.lastStart(t.getLastStart())
				.lastStop(t.getLastStop())
				.cronJob(t.getCronJob())
				.enabled(t.getEnabled())
				.build();
	}
	
	private Timer createEntity(TimerDto t) {
		
		return Timer.builder()
				.jobId(t.getJobId())
				.jobName(t.getJobName())
				.jobGroup(t.getJobGroup())
				.jobStatus(t.getJobStatus())
				.jobClass(t.getJobClass())
				.cronExpression(t.getCronExpression())
				.jobDescription(t.getJobDescription())
				.interfaceName(t.getInterfaceName())
				.cronJob(t.getCronJob() != null ? t.getCronJob() : 'Y')
				.enabled(t.getEnabled() != null ? t.getEnabled() : 'N')
				.build();
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.repository == null) ? 0 : this.repository.hashCode());
	    result = prime * result + ((this.schedulerService == null) ? 0 : this.schedulerService.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof TimerServiceImpl))
	        return false;
	    
	    TimerServiceImpl other = (TimerServiceImpl) obj;
	    boolean r = (this.repository == null && other.repository == null) 
	    		|| (this.repository != null && this.repository.equals(other.repository));
	    boolean s = (this.schedulerService == null && other.schedulerService == null) 
	    		|| (this.schedulerService != null && this.schedulerService.equals(other.schedulerService));
	    
	    return r && s;
	}
	
}
