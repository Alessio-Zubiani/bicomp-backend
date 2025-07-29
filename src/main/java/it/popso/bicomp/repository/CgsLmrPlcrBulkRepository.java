package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrPlcrBulk;

@Repository
public interface CgsLmrPlcrBulkRepository extends JpaRepository<CgsLmrPlcrBulk, BigDecimal> {

    Optional<CgsLmrPlcrBulk> findByBulkReference(String bulkReference);

}
