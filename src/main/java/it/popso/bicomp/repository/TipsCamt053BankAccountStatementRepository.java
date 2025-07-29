package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.TipsCamt053BankAccountStatement;

@Repository
public interface TipsCamt053BankAccountStatementRepository extends JpaRepository<TipsCamt053BankAccountStatement, BigDecimal> {
	
	@Query("SELECT t FROM TipsCamt053BankAccountStatement t WHERE t.settlementDate = ?1 ORDER BY t.creationDateTime ASC")
	List<TipsCamt053BankAccountStatement> findStatementByDate(Date settlementDate);
	
	@Query("SELECT t FROM TipsCamt053BankAccountStatement t WHERE t.settlementDate = ?1 ORDER BY t.creationDateTime DESC")
	Optional<TipsCamt053BankAccountStatement> findLastStatementByDate(Date settlementDate, PageRequest p);
	
	Optional<TipsCamt053BankAccountStatement> findById(BigDecimal id);
	
	@Query("SELECT t FROM TipsCamt053BankAccountStatement t WHERE t.settlementDate = ?1 ORDER BY t.creationDateTime DESC")
	Page<TipsCamt053BankAccountStatement> findBySettlementDate(Date settlementDate, Pageable p);
	
	@Query("SELECT t FROM TipsCamt053BankAccountStatement t WHERE t.settlementDate = ?1 ORDER BY t.creationDateTime ASC")
	List<TipsCamt053BankAccountStatement> findBySettlementDateNoPagination(Date settlementDate);
	
}
