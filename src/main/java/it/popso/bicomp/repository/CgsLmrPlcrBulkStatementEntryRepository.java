package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;

@Repository
public interface CgsLmrPlcrBulkStatementEntryRepository extends CrudRepository<CgsLmrPlcrBulkStatementEntry, BigDecimal>,
	PagingAndSortingRepository<CgsLmrPlcrBulkStatementEntry, BigDecimal>, 
	JpaSpecificationExecutor<CgsLmrPlcrBulkStatementEntry> {

	@Query("SELECT c FROM CgsLmrPlcrBulkStatementEntry c WHERE c.cgsLmrPlcrBulkStatement.cgsLmrPlcrBulk.cgsLmr.id = ?1 AND c.side = ?2 AND c.status = ?3 ORDER BY c.settlementDateTime ASC")
	Page<CgsLmrPlcrBulkStatementEntry> findByLmrIdAndSideAndStatus(BigDecimal lmrId, Character side, String status, Pageable paging);
	
	@Query("SELECT c FROM CgsLmrPlcrBulkStatementEntry c WHERE c.cgsLmrPlcrBulkStatement.cgsLmrPlcrBulk.cgsLmr.id = ?1 AND c.side = ?2 AND c.status IN ?3 ORDER BY c.settlementDateTime ASC")
	Page<CgsLmrPlcrBulkStatementEntry> findByLmrId(BigDecimal lmrId, Character side, List<String> status, Pageable paging);
	
	@Query("SELECT c FROM CgsLmrPlcrBulkStatementEntry c WHERE c.cgsLmrPlcrBulkStatement.cgsLmrPlcrBulk.cgsLmr.id = ?1 AND c.status IN ?2 ORDER BY c.settlementDateTime ASC")
	Page<CgsLmrPlcrBulkStatementEntry> findByLmrId(BigDecimal lmrId, List<String> status, Pageable paging);

	Optional<CgsLmrPlcrBulkStatementEntry> findByEntryReferenceAndSideAndCgsLmrPlcrBulkStatement_Id(String entryReference, Character side, BigDecimal id);

	@Query("SELECT c FROM CgsLmrPlcrBulkStatementEntry c WHERE c.cgsLmrPlcrBulkStatement.settlementDate = ?1 AND c.status = 'BOOK' ORDER BY c.settlementDateTime ASC")
	List<CgsLmrPlcrBulkStatementEntry> findBySettlementDate(Date date);
	
}
