package it.popso.bicomp.flyway;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FlywayMigration {
	
	@Bean
	public FlywayMigrationStrategy repairAndMigrateStrategy(@Qualifier("bicompDataSource") DataSource dataSource) {
		
		Flyway.configure()
        		.dataSource(dataSource)
        		.baselineOnMigrate(true)
        		.outOfOrder(true)
        		.ignoreMigrationPatterns("*:missing")
        		.load();
		
		return flyway -> {
			flyway.repair();
            flyway.migrate();
		};
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
}
