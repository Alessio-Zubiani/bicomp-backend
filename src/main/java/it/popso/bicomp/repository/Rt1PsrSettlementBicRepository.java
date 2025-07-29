package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Rt1PsrSettlementBic;

@Repository
public interface Rt1PsrSettlementBicRepository extends JpaRepository<Rt1PsrSettlementBic, BigDecimal> {
	
	@Query("SELECT r FROM Rt1PsrSettlementBic r WHERE r.rt1PsrFileHeader.settlementDate = ?1 ORDER BY r.rt1PsrFileHeader.lac DESC")
	Optional<Rt1PsrSettlementBic> findLastLacByDate(Date settlementDate, PageRequest p);
	
	@Query("SELECT r FROM Rt1PsrSettlementBic r WHERE r.rt1PsrFileHeader.settlementDate = ?1 ORDER BY r.rt1PsrFileHeader.lac ASC")
	List<Rt1PsrSettlementBic> findLacsByDate(Date settlementDate);

}
