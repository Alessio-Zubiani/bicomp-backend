package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Rt1Bulk;

@Repository
public interface Rt1BulkRepository extends JpaRepository<Rt1Bulk, BigDecimal> {

    Optional<Rt1Bulk> findByFileReferenceAndReportName(String fileReference, String reportName);

}
