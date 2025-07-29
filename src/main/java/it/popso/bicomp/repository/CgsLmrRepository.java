package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmr;

@Repository
public interface CgsLmrRepository extends JpaRepository<CgsLmr, BigDecimal> {

	Optional<CgsLmr> findByFileRef(String fileRef);
	
	@Query("SELECT c FROM CgsLmr c WHERE c.fileSettlementDate = ?1 ORDER BY c.fileLac DESC")
	Optional<CgsLmr> findLastStatementByDate(Date date, PageRequest p);
	
	@Query("SELECT c FROM CgsLmr c WHERE c.fileSettlementDate = ?1 ORDER BY c.fileLac ASC")
	public List<CgsLmr> findByFileSettlementDate(Date date);

}
