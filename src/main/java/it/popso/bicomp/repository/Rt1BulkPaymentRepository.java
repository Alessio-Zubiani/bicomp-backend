package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Rt1BulkPayment;

@Repository
public interface Rt1BulkPaymentRepository extends CrudRepository<Rt1BulkPayment, BigDecimal>, 
	PagingAndSortingRepository<Rt1BulkPayment, BigDecimal>, 
	JpaSpecificationExecutor<Rt1BulkPayment> {

	@Query("SELECT r.side, r.status, SUM(r.paymentAmount) FROM Rt1BulkPayment r WHERE r.settlementDate = ?1 AND r.rt1Bulk.fileCycle = ?2 AND r.originalMsgName LIKE 'pacs.%' GROUP BY r.side, r.status")
	List<Object[]> findTotalBySettlementDateAndLac(Date settlementDate, String lac);
	
	Optional<Rt1BulkPayment> findByMsgId(String msgId);

}
