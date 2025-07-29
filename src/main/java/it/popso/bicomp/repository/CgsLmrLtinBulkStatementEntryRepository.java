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

import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;

@Repository
public interface CgsLmrLtinBulkStatementEntryRepository extends CrudRepository<CgsLmrLtinBulkStatementEntry, BigDecimal>, 
	PagingAndSortingRepository<CgsLmrLtinBulkStatementEntry, BigDecimal>, 
	JpaSpecificationExecutor<CgsLmrLtinBulkStatementEntry>{

	Optional<CgsLmrLtinBulkStatementEntry> findByEntryReference(String entryReference);

	@Query("SELECT c FROM CgsLmrLtinBulkStatementEntry c WHERE c.cgsLmrLtinBulkStatement.cgsLmrLtinBulk.cgsLmr.id = ?1 AND c.side = ?2 ORDER BY c.settlementDateTime ASC")
	Page<CgsLmrLtinBulkStatementEntry> findByLmrIdAndSide(BigDecimal lmrId, Character side, Pageable paging);
	
	@Query("SELECT c FROM CgsLmrLtinBulkStatementEntry c WHERE c.cgsLmrLtinBulkStatement.cgsLmrLtinBulk.cgsLmr.id = ?1 AND c.side = ?2 ORDER BY c.settlementDateTime ASC")
	Page<CgsLmrLtinBulkStatementEntry> findByLmrId(BigDecimal lmrId, Character side, Pageable paging);
	
	@Query("SELECT c FROM CgsLmrLtinBulkStatementEntry c WHERE c.cgsLmrLtinBulkStatement.settlementDate = ?1 AND c.status = 'BOOK' ORDER BY c.settlementDateTime ASC")
	List<CgsLmrLtinBulkStatementEntry> findBySettlementDate(Date date);
	
}
