package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;

@Repository
public interface TipsCamt053BankAccountStatementEntryRepository extends JpaRepository<TipsCamt053BankAccountStatementEntry, BigDecimal>, 
	JpaSpecificationExecutor<TipsCamt053BankAccountStatementEntry> {
	
	@Query("SELECT t.side, SUM(t.paymentAmount) FROM TipsCamt053BankAccountStatementEntry t WHERE t.tipsCamt053BankAccountStatement.id = ?1 GROUP BY t.side")
	List<Object[]> findTotalByReportId(BigDecimal id);
	
	@Query("SELECT t FROM TipsCamt053BankAccountStatementEntry t WHERE t.tipsCamt053BankAccountStatement.id = ?1 AND t.side = ?2 ORDER BY t.settlementDateTime ASC")
	Page<TipsCamt053BankAccountStatementEntry> findByAccountStatementId(BigDecimal reportId, Character side, Pageable paging);
	
	@Query("SELECT t FROM TipsCamt053BankAccountStatementEntry t WHERE t.tipsCamt053BankAccountStatement.settlementDate = ?1 ORDER BY t.settlementDateTime DESC")
	Page<TipsCamt053BankAccountStatementEntry> findLastPaymentBySettlementDate(Date date, Pageable pageable);
	
	Optional<TipsCamt053BankAccountStatementEntry> findByEntryReferenceAndSide(String entryReference, Character side);

	@Query("SELECT t FROM TipsCamt053BankAccountStatementEntry t WHERE t.tipsCamt053BankAccountStatement.settlementDate = ?1 ORDER BY t.settlementDateTime ASC")
	List<TipsCamt053BankAccountStatementEntry> findBySettlementDate(Date settlementDate);

}