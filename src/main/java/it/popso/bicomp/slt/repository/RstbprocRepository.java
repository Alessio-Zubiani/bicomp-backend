package it.popso.bicomp.slt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.slt.model.Rstbproc;
import it.popso.bicomp.slt.model.RstbprocPK;

@Repository
public interface RstbprocRepository extends JpaRepository<Rstbproc, RstbprocPK> {
	
	@Query("SELECT r FROM Rstbproc r WHERE r.id.procTipoVoce = ?1")
	Optional<Rstbproc> findByProcTipoVoce(String procTipoVoce);

}
