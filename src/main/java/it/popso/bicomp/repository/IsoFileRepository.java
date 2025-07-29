package it.popso.bicomp.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.IsoFile;

@Repository
public interface IsoFileRepository extends JpaRepository<IsoFile, BigDecimal> {
	
}
