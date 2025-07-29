package it.popso.bicomp.quartz.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import it.popso.bicomp.properties.YamlPropertySourceFactory;


@Configuration
@PropertySource(value = "file:/app/config/application.yaml", factory = YamlPropertySourceFactory.class)
//@PropertySource(value = "file:src/main/resources/application.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "org.quartz")
public class SchedulerConfig {
    
    @Bean
	public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("bicompDataSource") DataSource dataSource, 
			QuartzProperties quartzProperties, SchedulerJobFactory schedulerJobFactory) {
    	
    	Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
    	
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setOverwriteExistingJobs(true);
		schedulerFactoryBean.setDataSource(dataSource);
		schedulerFactoryBean.setQuartzProperties(properties);
		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
		schedulerFactoryBean.setJobFactory(schedulerJobFactory);
		
		return schedulerFactoryBean;
	}
	
}
