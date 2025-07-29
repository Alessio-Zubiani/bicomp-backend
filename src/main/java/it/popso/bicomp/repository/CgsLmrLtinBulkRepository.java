package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrLtinBulk;

@Repository
public interface CgsLmrLtinBulkRepository extends JpaRepository<CgsLmrLtinBulk, BigDecimal> {

    Optional<CgsLmrLtinBulk> findByBulkReference(String bulkReference);

}
