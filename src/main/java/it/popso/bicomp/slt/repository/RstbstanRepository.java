package it.popso.bicomp.slt.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.slt.model.Rstbstan;
import it.popso.bicomp.slt.model.RstbstanPK;

@Repository
public interface RstbstanRepository extends JpaRepository<Rstbstan, RstbstanPK> {
	
	@Query("SELECT r FROM Rstbstan r WHERE r.id.stanData = ?1 AND r.id.stanMessaggio = ?2 AND r.id.stanNumeroCicli = ?3 ORDER BY r.id.stanTipoVoce ASC")
	List<Rstbstan> findByStanDataAndStanMessaggioAndStanNumeroCicli(Date stanData, String stanMessaggio, BigDecimal stanNumeroCicli);

}
