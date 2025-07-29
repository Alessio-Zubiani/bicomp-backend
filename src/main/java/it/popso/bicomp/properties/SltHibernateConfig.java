package it.popso.bicomp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Component
@PropertySource(value = "file:/app/config/application.yaml", factory = YamlPropertySourceFactory.class)
//@PropertySource(value = "file:src/main/resources/application.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "slt")
public class SltHibernateConfig extends HibernateBaseEntity {
	
	@Builder
	public SltHibernateConfig(Datasource datasource, Hibernate hibernate) {
		super(datasource, hibernate);
	}

}
