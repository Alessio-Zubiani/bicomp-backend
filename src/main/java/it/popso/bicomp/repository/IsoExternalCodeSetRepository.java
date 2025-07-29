package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.IsoExternalCodeSet;

@Repository
public interface IsoExternalCodeSetRepository extends JpaRepository<IsoExternalCodeSet, BigDecimal> {
	
	@Query("SELECT i FROM IsoExternalCodeSet i WHERE i.codeValue LIKE %?1%")
	List<IsoExternalCodeSet> findByCodeValue(String codeValue);
	
}
