package it.popso.bicomp;

import java.util.Objects;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.service.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
@RequiredArgsConstructor
public class BicompStartedListener implements ApplicationListener<ApplicationStartedEvent> {
	
	private final TimerService timerService;
	private final SchedulerFactoryBean schedulerFactoryBean;
	
	
	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		
		log.info("Deleting old job from scheduler");
		try {
			Scheduler scheduler = this.schedulerFactoryBean.getScheduler();
			
			log.info("Before CLEAR....");
			this.getJobs(scheduler);
			scheduler.clear();
			
			log.info("After CLEAR....");
			this.getJobs(scheduler);
			
			log.info("Starting job after STARTUP");
			this.timerService.scheduleTimer();
		} 
		catch (BicompException | SchedulerException e) {
			log.error("Error: ", e);
		}
	}
	
	private void getJobs(Scheduler scheduler) throws SchedulerException {
		for (String groupName : scheduler.getJobGroupNames()) {
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
				String jobName = jobKey.getName();

				log.info("[jobName] : [{}] - [groupName] : [{}]", jobName, groupName);
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(schedulerFactoryBean, timerService);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BicompStartedListener other = (BicompStartedListener) obj;
		return Objects.equals(schedulerFactoryBean, other.schedulerFactoryBean)
				&& Objects.equals(timerService, other.timerService);
	}

}
