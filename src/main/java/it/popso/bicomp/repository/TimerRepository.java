package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Timer;

@Repository
public interface TimerRepository extends JpaRepository<Timer, BigDecimal> {

	List<Timer> findAllByOrderByJobNameAsc();
	
	Optional<Timer> findByJobName(String jobName);
	
	Optional<Timer> findByJobClass(String jobClass);
	
	@Query("SELECT t FROM Timer t WHERE t.enabled = 'Y'")
	List<Timer> findEnabledTimer();
	
}
