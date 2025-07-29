package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrLacBulk;

@Repository
public interface CgsLmrLacBulkRepository extends JpaRepository<CgsLmrLacBulk, BigDecimal> {

    Optional<CgsLmrLacBulk> findByBulkReference(String bulkReference);

}
