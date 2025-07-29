package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.FeedbackEventi;

@Repository
public interface FeedbackEventiRepository extends JpaRepository<FeedbackEventi, BigDecimal> {
	
	List<FeedbackEventi> findByFeedbackIdOrderByTmsInsertDesc(BigDecimal id);
	
}
