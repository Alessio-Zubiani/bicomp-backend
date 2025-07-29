package it.popso.bicomp.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Rt1PsrParticipantHeader;

@Repository
public interface Rt1PsrParticipantHeaderRepository extends JpaRepository<Rt1PsrParticipantHeader, BigDecimal> {

}
