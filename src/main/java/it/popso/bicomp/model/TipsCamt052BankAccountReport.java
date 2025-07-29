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
import java.util.Date;


/**
 * The persistent class for the TIPS_CAMT_052_BANK_ACCOUNT_REPORT database table.
 * 
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name = "TIPS_CAMT_052_BANK_ACCOUNT_REPORT")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class TipsCamt052BankAccountReport extends TipsBankEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
	public TipsCamt052BankAccountReport(BigDecimal id, Date tmsInsert, Date creationDateTime, String accountId, 
			String accountOwner, BigDecimal closingBalance, Character closingBalanceSide, String currency, 
			Character flagElaborato, String msgId, BigDecimal openingBalance, Character openingBalanceSide, 
			String reportName, Date settlementDate, Date tmsUpdate, BigDecimal totalCreditOperation, BigDecimal totalDebitOperation) {
		super(id, tmsInsert, creationDateTime);
		this.accountId = accountId;
		this.accountOwner = accountOwner;
		this.closingBalance = closingBalance;
		this.closingBalanceSide = closingBalanceSide;
		this.currency = currency;
		this.flagElaborato = flagElaborato;
		this.msgId = msgId;
		this.reportName = reportName;
		this.openingBalance = openingBalance;
		this.openingBalanceSide = openingBalanceSide;
		this.settlementDate = settlementDate;
		this.tmsUpdate = tmsUpdate;
		this.totalCreditOperation = totalCreditOperation;
		this.totalDebitOperation = totalDebitOperation;
	}

	@Column(name = "ACCOUNT_ID")
	private String accountId;

	@Column(name = "ACCOUNT_OWNER")
	private String accountOwner;

	@Column(name = "CLOSING_BALANCE")
	private BigDecimal closingBalance;
	
	@Column(name = "CLOSING_BALANCE_SIDE")
	private Character closingBalanceSide;

	@Column(name = "CURRENCY")
	private String currency;
	
	@Column(name = "FLAG_ELABORATO")
	private Character flagElaborato;

	@Column(name = "MSG_ID")
	private String msgId;

	@Column(name = "OPENING_BALANCE")
	private BigDecimal openingBalance;
	
	@Column(name = "OPENING_BALANCE_SIDE")
	private Character openingBalanceSide;

	@Column(name = "REPORT_NAME")
	private String reportName;

	@Temporal(TemporalType.DATE)
	@Column(name = "SETTLEMENT_DATE")
	private Date settlementDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_UPDATE")
	private Date tmsUpdate;

	@Column(name = "TOTAL_CREDIT_OPERATION")
	private BigDecimal totalCreditOperation;

	@Column(name = "TOTAL_DEBIT_OPERATION")
	private BigDecimal totalDebitOperation;

}