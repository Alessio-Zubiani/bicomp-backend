package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, BigDecimal> {
	
	List<Feedback> findAllByOrderByTmsInsertDesc();
	
}
