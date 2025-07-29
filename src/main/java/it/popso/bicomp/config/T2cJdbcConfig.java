package it.popso.bicomp.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.popso.bicomp.properties.T2cHibernateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		entityManagerFactoryRef = "t2cEntityManager",
        transactionManagerRef = "t2cTransactionManager",
        basePackages = {
    		"it.popso.bicomp.t2c.repository"
        })
@RequiredArgsConstructor
public class T2cJdbcConfig {
	
	private final T2cHibernateConfig config;
	
	
	@Bean(name = "t2cDataSource")
	public DataSource t2cDataSource() {
		
		log.info("Connecting to schema: [{}]", this.config.getDatasource().getUsername());
		
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(this.config.getDatasource().getUrl());
		dataSource.setDriverClassName(this.config.getDatasource().getDriver());
		dataSource.setUsername(this.config.getDatasource().getUsername());
		dataSource.setPassword(this.config.getDatasource().getPassword());
		
		return dataSource;
	}

	@Bean(name = "t2cEntityManager")
	public LocalContainerEntityManagerFactoryBean t2cEntityManager(EntityManagerFactoryBuilder builder, @Qualifier("t2cDataSource") DataSource t2cDataSource) {
		
		Map<String, String> properties = new HashMap<>();
		properties.put("hibernate.show_sql", this.config.getHibernate().getShowSql());
		properties.put("hibernate.format_sql", this.config.getHibernate().getFormatSql());
		
		return builder
				.dataSource(t2cDataSource)
				.properties(properties)
				.packages("it.popso.bicomp.t2c.model")
				.persistenceUnit("t2c")
				.build();
	}
	
	@Bean(name = "t2cTransactionManager")
	public PlatformTransactionManager t2cTransactionManager(@Qualifier("t2cEntityManager") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(config);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		T2cJdbcConfig other = (T2cJdbcConfig) obj;
		return Objects.equals(config, other.config);
	}
	
}
