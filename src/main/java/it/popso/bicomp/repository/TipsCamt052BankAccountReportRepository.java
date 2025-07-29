package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.TipsCamt052BankAccountReport;

@Repository
public interface TipsCamt052BankAccountReportRepository extends JpaRepository<TipsCamt052BankAccountReport, BigDecimal> {
	
	List<TipsCamt052BankAccountReport> findByFlagElaboratoOrderBySettlementDateAsc(Character flagElaborato);
	
	Optional<TipsCamt052BankAccountReport> findByMsgId(String messageId);

	Optional<TipsCamt052BankAccountReport> findByReportName(String reportName);
	
	@Modifying
	@Query("UPDATE TipsCamt052BankAccountReport t SET t.flagElaborato = :flagElaborato, t.tmsUpdate = SYSTIMESTAMP WHERE t.msgId = :msgId")
	void updateFlagElaborato(@Param("msgId") String messageId, @Param("flagElaborato") Character flagElaborato);
	
}