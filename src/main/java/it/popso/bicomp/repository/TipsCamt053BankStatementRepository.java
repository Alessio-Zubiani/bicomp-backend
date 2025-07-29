package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.TipsCamt053BankStatement;

@Repository
public interface TipsCamt053BankStatementRepository extends JpaRepository<TipsCamt053BankStatement, BigDecimal> {
	
	Optional<TipsCamt053BankStatement> findByMsgId(String msgId);
	
}
