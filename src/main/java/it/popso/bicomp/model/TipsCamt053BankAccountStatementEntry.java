package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the TIPS_CAMT_053_BANK_ACCOUNT_STATEMENT_ENTRY database table.
 * 
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "TIPS_CAMT_053_BANK_ACCOUNT_STATEMENT_ENTRY", uniqueConstraints = {
	@UniqueConstraint(name = "TIPS_CAMT_053_BANK_ACCOUNT_STATEMENT_ENTRY_ENTRY_REFERENCE_IDX", columnNames = {"ENTRY_REFERENCE"})
})

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class TipsCamt053BankAccountStatementEntry extends TipsBaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
	public TipsCamt053BankAccountStatementEntry(BigDecimal id, Date tmsInsert, String bankTransactionCode, 
			String bankTransactionCodeFamily, String creditorBic, String currency, String debitorBic, 
			String entryReference, BigDecimal paymentAmount, Date settlementDateTime, Character side, 
			String status, TipsCamt053BankAccountStatement tipsCamt053BankAccountStatement) {
		super(id, tmsInsert);
		this.bankTransactionCode = bankTransactionCode;
		this.bankTransactionCodeFamily = bankTransactionCodeFamily;
		this.creditorBic = creditorBic;
		this.currency = currency;
		this.debitorBic = debitorBic;
		this.entryReference = entryReference;
		this.paymentAmount = paymentAmount;
		this.settlementDateTime = settlementDateTime;
		this.side = side;
		this.status = status;
		this.tipsCamt053BankAccountStatement = tipsCamt053BankAccountStatement;
	}
	
	@Column(name = "BANK_TRANSACTION_CODE")
	private String bankTransactionCode;

	@Column(name = "BANK_TRANSACTION_CODE_FAMILY")
	private String bankTransactionCodeFamily;

	@Column(name = "CREDITOR_BIC")
	private String creditorBic;

	@Column(name = "CURRENCY")
	private String currency;

	@Column(name = "DEBITOR_BIC")
	private String debitorBic;

	@Column(name = "ENTRY_REFERENCE")
	private String entryReference;

	@Column(name = "PAYMENT_AMOUNT")
	private BigDecimal paymentAmount;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SETTLEMENT_DATE_TIME")
	private Date settlementDateTime;

	@Column(name = "SIDE")
	private Character side;

	@Column(name = "STATUS")
	private String status;

	//bi-directional many-to-one association to TipsCamt053BankAccountStatement
	@ManyToOne
	@JoinColumn(name = "BANK_ACCOUNT_STATEMENT_ID")
	private TipsCamt053BankAccountStatement tipsCamt053BankAccountStatement;

}