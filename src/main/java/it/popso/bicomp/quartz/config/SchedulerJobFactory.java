package it.popso.bicomp.quartz.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.AdaptableJobFactory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SchedulerJobFactory extends AdaptableJobFactory {

	private final AutowireCapableBeanFactory capableBeanFactory;
	
	@Override
	protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
		//Call method of the parent class
		Object jobInstance = super.createJobInstance(bundle);
		//Spring injection
		this.capableBeanFactory.autowireBean(jobInstance);
		return jobInstance;
	}
    
}
