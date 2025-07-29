package it.popso.bicomp.quartz.config;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class JobScheduleCreator {
	
	public CronTrigger createCronTrigger(String triggerName, Date startTime, String cronExpression, int misFireInstruction) throws ParseException {
	    
		CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
	    factoryBean.setName(triggerName);
	    factoryBean.setStartTime(startTime);
	    factoryBean.setCronExpression(cronExpression);
	    factoryBean.setMisfireInstruction(misFireInstruction);
		factoryBean.afterPropertiesSet();
		
	    return factoryBean.getObject();
	}
	
	public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable, ApplicationContext context, String jobName, String jobGroup) {
		
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(jobClass);
		factoryBean.setDurability(isDurable);
		factoryBean.setApplicationContext(context);
		factoryBean.setName(jobName);
		factoryBean.setGroup(jobGroup);
		
		// Set job data map
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("jobName", jobName);
		factoryBean.setJobDataMap(jobDataMap);
		factoryBean.afterPropertiesSet();
		
		return factoryBean.getObject();
	}

}
