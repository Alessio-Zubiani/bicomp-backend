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


/**
 * The persistent class for the CGS_LMR_LAC_BULK_STATEMENT database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_LAC_BULK_STATEMENT")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrLacBulkStatement extends CgsStatementEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
    public CgsLmrLacBulkStatement(BigDecimal id, Date tmsInsert, String accountOwner, Timestamp creationDateTime, 
    		BigDecimal closingBalance, Character closingBalanceSide, BigDecimal creditLiquidityTransfer, 
    		BigDecimal creditPayments, BigDecimal debitLiquidityTransfer, BigDecimal debitPayments, Timestamp fromDateTime, 
    		BigDecimal openingBalance, Character openingBalanceSide, Date settlementDate, String statementReference, 
    		Timestamp toDateTime, CgsLmrLacBulk cgsLmrLacBulk) {
        super(id, tmsInsert, accountOwner, creationDateTime, fromDateTime, settlementDate, statementReference, toDateTime);
        this.closingBalance = closingBalance;
        this.closingBalanceSide = closingBalanceSide;
        this.creditLiquidityTransfer = creditLiquidityTransfer;
        this.creditPayments = creditPayments;
        this.debitLiquidityTransfer = debitLiquidityTransfer;
        this.debitPayments = debitPayments;
        this.openingBalance = openingBalance;
        this.openingBalanceSide = openingBalanceSide;
        this.cgsLmrLacBulk = cgsLmrLacBulk;
    }

	@Column(name = "CLOSING_BALANCE")
	private BigDecimal closingBalance;

	@Column(name = "CLOSING_BALANCE_SIDE")
	private Character closingBalanceSide;

	@Column(name = "CREDIT_LIQUIDITY_TRANSFER")
	private BigDecimal creditLiquidityTransfer;

	@Column(name = "CREDIT_PAYMENTS")
	private BigDecimal creditPayments;

	@Column(name = "DEBIT_LIQUIDITY_TRANSFER")
	private BigDecimal debitLiquidityTransfer;

	@Column(name = "DEBIT_PAYMENTS")
	private BigDecimal debitPayments;

	@Column(name = "OPENING_BALANCE")
	private BigDecimal openingBalance;

	@Column(name = "OPENING_BALANCE_SIDE")
	private Character openingBalanceSide;

	//bi-directional one-to-one association to CgsLmrLacBulk
	@ToString.Exclude
	@OneToOne
	@JoinColumn(name = "LMR_LAC_BULK_ID")
	private CgsLmrLacBulk cgsLmrLacBulk;

}