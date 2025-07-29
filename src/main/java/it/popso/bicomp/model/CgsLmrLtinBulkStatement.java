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
 * The persistent class for the CGS_LMR_LTIN_BULK_STATEMENT database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_LTIN_BULK_STATEMENT")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrLtinBulkStatement extends CgsStatementEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
    public CgsLmrLtinBulkStatement(BigDecimal id, Date tmsInsert, String accountOwner, Timestamp creationDateTime, 
    		BigDecimal creditLiquidityTransfer, BigDecimal debitLiquidityTransfer, Timestamp fromDateTime, 
    		Date settlementDate, String statementReference, Timestamp toDateTime, CgsLmrLtinBulk cgsLmrLtinBulk, 
    		List<CgsLmrLtinBulkStatementEntry> cgsLmrLtinBulkStatementEntries) {
		super(id, tmsInsert, accountOwner, creationDateTime, fromDateTime, settlementDate, statementReference, toDateTime);
        this.creditLiquidityTransfer = creditLiquidityTransfer;
        this.debitLiquidityTransfer = debitLiquidityTransfer;
        this.cgsLmrLtinBulk = cgsLmrLtinBulk;
        this.cgsLmrLtinBulkStatementEntries = cgsLmrLtinBulkStatementEntries;
    }

	@Column(name = "CREDIT_LIQUIDITY_TRANSFER")
	private BigDecimal creditLiquidityTransfer;

	@Column(name = "DEBIT_LIQUIDITY_TRANSFER")
	private BigDecimal debitLiquidityTransfer;

	//bi-directional one-to-one association to CgsLmrLtinBulk
	@OneToOne
	@JoinColumn(name = "LMR_LTIN_BULK_ID")
	@ToString.Exclude
	private CgsLmrLtinBulk cgsLmrLtinBulk;

	//bi-directional many-to-one association to CgsLmrLtinBulkStatementEntry
	@OneToMany(mappedBy = "cgsLmrLtinBulkStatement")
	private List<CgsLmrLtinBulkStatementEntry> cgsLmrLtinBulkStatementEntries;

}