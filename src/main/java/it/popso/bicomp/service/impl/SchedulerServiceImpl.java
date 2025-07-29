package it.popso.bicomp.service.impl;

import java.text.ParseException;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.quartz.config.JobScheduleCreator;
import it.popso.bicomp.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {
	
	private final SchedulerFactoryBean schedulerFactoryBean;
	private final ApplicationContext context;
	private final JobScheduleCreator scheduleCreator;
	

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void enable(Timer t) throws BicompException {
		
		try {
			Scheduler scheduler = this.schedulerFactoryBean.getScheduler();
			JobDetail jobDetail = JobBuilder
					.newJob((Class<? extends QuartzJobBean>) Class.forName(t.getJobClass()))
					.withIdentity(t.getJobName(), t.getJobGroup()).build();
			
			if(!scheduler.checkExists(jobDetail.getKey())) {
	
				jobDetail = this.scheduleCreator.createJob(
						(Class<? extends QuartzJobBean>) Class.forName(t.getJobClass()), false, this.context,
						t.getJobName(), t.getJobGroup());
	
				Trigger trigger = this.scheduleCreator.createCronTrigger(
						t.getJobName(), 
						new Date(),
						t.getCronExpression(),
						SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
				
				scheduler.scheduleJob(jobDetail, trigger);
			} 
			else {
				log.info("Job already exist [{}]", t.getJobName());
				throw new BicompException(new StringBuilder("[").append(t.getJobName()).append("] already exists in scheduler").toString());
			}
		}
		catch(SchedulerException | ClassNotFoundException | ParseException e) {
			throw new BicompException(e.getMessage());
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void disable(Timer t) throws BicompException {
		
		try {
			Scheduler scheduler = this.schedulerFactoryBean.getScheduler();
	        scheduler.deleteJob(new JobKey(t.getJobName(), t.getJobGroup()));
		}
		catch (SchedulerException e) {
	        throw new BicompException(e.getMessage());
	    }
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void updateScheduling(Timer t) throws BicompException {
		
		try {
			Scheduler scheduler = this.schedulerFactoryBean.getScheduler();
			Trigger newTrigger = this.scheduleCreator.createCronTrigger(
				        t.getJobName(), 
				        new Date(), 
				        t.getCronExpression(), 
				        SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
			
			scheduler.rescheduleJob(TriggerKey.triggerKey(t.getJobName()), newTrigger);
		}
		catch (SchedulerException | ParseException e) {
			throw new BicompException(e.getMessage());
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void runNow(Timer t) {
		
		try {
			Scheduler scheduler = this.schedulerFactoryBean.getScheduler();
	        scheduler.triggerJob(new JobKey(t.getJobName(), t.getJobGroup()));
		}
		catch (SchedulerException e) {
			throw new BicompException(e.getMessage());
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void unscheduleAndDelete(Timer t) {
		
		try {
			Scheduler scheduler = this.schedulerFactoryBean.getScheduler();
			scheduler.deleteJob(new JobKey(t.getJobName(), t.getJobGroup()));
		}
		catch (SchedulerException e) {
			throw new BicompException(e.getMessage());
		}
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.schedulerFactoryBean == null) ? 0 : this.schedulerFactoryBean.hashCode());
	    result = prime * result + ((this.context == null) ? 0 : this.context.hashCode());
	    result = prime * result + ((this.scheduleCreator == null) ? 0 : this.scheduleCreator.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof SchedulerServiceImpl))
	        return false;
	    
	    SchedulerServiceImpl other = (SchedulerServiceImpl) obj;
	    boolean factory = (this.schedulerFactoryBean == null && other.schedulerFactoryBean == null) 
	    		|| (this.schedulerFactoryBean != null && this.schedulerFactoryBean.equals(other.schedulerFactoryBean));
	    boolean c = (this.context == null && other.context == null) 
	    		|| (this.context != null && this.context.equals(other.context));
	    boolean creator = (this.scheduleCreator == null && other.scheduleCreator == null) 
	    		|| (this.scheduleCreator != null && this.scheduleCreator.equals(other.scheduleCreator));
	    
	    return factory && c && creator;
	}

}
