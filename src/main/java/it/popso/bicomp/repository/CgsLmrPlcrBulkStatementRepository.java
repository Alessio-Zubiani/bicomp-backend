package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrPlcrBulkStatement;

@Repository
public interface CgsLmrPlcrBulkStatementRepository extends JpaRepository<CgsLmrPlcrBulkStatement, BigDecimal> {

	@Query("SELECT c FROM CgsLmrPlcrBulkStatement c WHERE c.cgsLmrPlcrBulk.cgsLmr.id = ?1")
	public List<CgsLmrPlcrBulkStatement> findByLmrId(BigDecimal lmrId);

	Optional<CgsLmrPlcrBulkStatement> findByStatementReference(String statementReference);
	
}
