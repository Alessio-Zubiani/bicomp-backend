package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Rt1PsrFileHeader;

@Repository
public interface Rt1PsrFileHeaderRepository extends JpaRepository<Rt1PsrFileHeader, BigDecimal> {

    Optional<Rt1PsrFileHeader> findByReportNameAndSenderFileReference(String reportName, String senderFileReference);

}
