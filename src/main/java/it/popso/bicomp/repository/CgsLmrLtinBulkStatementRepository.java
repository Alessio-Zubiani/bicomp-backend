package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.CgsLmrLtinBulkStatement;

@Repository
public interface CgsLmrLtinBulkStatementRepository extends JpaRepository<CgsLmrLtinBulkStatement, BigDecimal> {

    Optional<CgsLmrLtinBulkStatement> findByStatementReference(String statementReference);

}
