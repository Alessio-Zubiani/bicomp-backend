package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrLacBulkStatement;

@Repository
public interface CgsLmrLacBulkStatementRepository extends JpaRepository<CgsLmrLacBulkStatement, BigDecimal> {
	
	@Query("SELECT c FROM CgsLmrLacBulkStatement c WHERE c.cgsLmrLacBulk.cgsLmr.id = ?1")
	public Optional<CgsLmrLacBulkStatement> findByLmrId(BigDecimal lmrId);
	
	@Query("SELECT c FROM CgsLmrLacBulkStatement c WHERE c.settlementDate = ?1 ORDER BY c.creationDateTime ASC")
	public List<CgsLmrLacBulkStatement> findBySettlementDate(Date date);
	
	@Query("SELECT c FROM CgsLmrLacBulkStatement c WHERE c.settlementDate = ?1 ORDER BY c.creationDateTime DESC")
	public Page<CgsLmrLacBulkStatement> findBySettlementDate(Date settlementDate, Pageable pageable);

	Optional<CgsLmrLacBulkStatement> findByStatementReference(String statementReference);

}
