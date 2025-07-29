package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Rt1PsrParticipantBody;

@Repository
public interface Rt1PsrParticipantBodyRepository extends CrudRepository<Rt1PsrParticipantBody, BigDecimal>, 
	PagingAndSortingRepository<Rt1PsrParticipantBody, BigDecimal>, 
	JpaSpecificationExecutor<Rt1PsrParticipantBody> {
	
	@Query("SELECT r FROM Rt1PsrParticipantBody r WHERE r.rt1PsrParticipantHeader.rt1PsrSettlementBic.id = ?1")
	List<Rt1PsrParticipantBody> findDailyLiquidityTransfer(BigDecimal id);

}
