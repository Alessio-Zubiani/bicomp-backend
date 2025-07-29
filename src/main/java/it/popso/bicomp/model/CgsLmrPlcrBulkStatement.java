package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the CGS_LMR_PLCR_BULK_STATEMENT database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_PLCR_BULK_STATEMENT")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrPlcrBulkStatement extends CgsStatementEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
    public CgsLmrPlcrBulkStatement(BigDecimal id, Date tmsInsert, String accountOwner, Timestamp creationDateTime, 
    		BigDecimal creditPayments, BigDecimal debitPayments, Timestamp fromDateTime, 
    		BigDecimal pendingPayments, String service, Date settlementDate, String statementReference, 
    		Timestamp toDateTime, CgsLmrPlcrBulk cgsLmrPlcrBulk, List<CgsLmrPlcrBulkStatementEntry> cgsLmrPlcrBulkStatementEntries) {
		super(id, tmsInsert, accountOwner, creationDateTime, fromDateTime, settlementDate, statementReference, toDateTime);
        this.creditPayments = creditPayments;
        this.debitPayments = debitPayments;
        this.pendingPayments = pendingPayments;
        this.service = service;
        this.cgsLmrPlcrBulk = cgsLmrPlcrBulk;
        this.cgsLmrPlcrBulkStatementEntries = cgsLmrPlcrBulkStatementEntries;
    }

	@Column(name = "CREDIT_PAYMENTS")
	private BigDecimal creditPayments;

	@Column(name = "DEBIT_PAYMENTS")
	private BigDecimal debitPayments;

	@Column(name = "PENDING_PAYMENTS")
	private BigDecimal pendingPayments;
	
	@Column(name = "SERVICE")
	private String service;

	//bi-directional many-to-one association to CgsLmrPlcrBulk
	@ManyToOne
	@JoinColumn(name = "LMR_PLCR_BULK_ID")
	@ToString.Exclude
	private CgsLmrPlcrBulk cgsLmrPlcrBulk;

	//bi-directional many-to-one association to CgsLmrPlcrBulkStatementEntry
	@OneToMany(mappedBy = "cgsLmrPlcrBulkStatement")
	private List<CgsLmrPlcrBulkStatementEntry> cgsLmrPlcrBulkStatementEntries;

}