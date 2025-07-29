package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the TIPS_CAMT_053_BANK_ACCOUNT_STATEMENT database table.
 * 
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name = "TIPS_CAMT_053_BANK_ACCOUNT_STATEMENT")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class TipsCamt053BankAccountStatement extends TipsBankEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
	public TipsCamt053BankAccountStatement(BigDecimal id, Date tmsInsert, Date creationDateTime, String accountNumber, 
			String accountOwner, Character openingBalanceIndicator, BigDecimal openingBalance, Character closingBalanceIndicator, 
			BigDecimal closingBalance, String currency, Date fromDateTime, Date settlementDate, String stmtId, Date toDateTime, 
			TipsCamt053BankStatement tipsCamt053BankStatement, List<TipsCamt053BankAccountStatementEntry> tipsCamt053BankAccountStatementEntries) {
		super(id, tmsInsert, creationDateTime);
		this.accountNumber = accountNumber;
		this.accountOwner = accountOwner;
		this.openingBalanceIndicator = openingBalanceIndicator;
		this.openingBalance = openingBalance;
		this.closingBalanceIndicator = closingBalanceIndicator;
		this.closingBalance = closingBalance;
		this.currency = currency;
		this.fromDateTime = fromDateTime;
		this.settlementDate = settlementDate;
		this.stmtId = stmtId;
		this.toDateTime = toDateTime;
		this.tipsCamt053BankStatement = tipsCamt053BankStatement;
		this.tipsCamt053BankAccountStatementEntries = tipsCamt053BankAccountStatementEntries;
	}

	@Column(name = "ACCOUNT_NUMBER")
	private String accountNumber;

	@Column(name = "ACCOUNT_OWNER")
	private String accountOwner;

	@Column(name = "OPENING_BALANCE")
	private BigDecimal openingBalance;
	
	@Column(name = "OPENING_BALANCE_INDICATOR")
	private Character openingBalanceIndicator;
	
	@Column(name = "CLOSING_BALANCE")
	private BigDecimal closingBalance;

	@Column(name = "CLOSING_BALANCE_INDICATOR")
	private Character closingBalanceIndicator;
	
	@Column(name = "CURRENCY")
	private String currency;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FROM_DATE_TIME")
	private Date fromDateTime;

	@Temporal(TemporalType.DATE)
	@Column(name = "SETTLEMENT_DATE")
	private Date settlementDate;

	@Column(name = "STMT_ID")
	private String stmtId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TO_DATE_TIME")
	private Date toDateTime;

	//bi-directional many-to-one association to TipsCamt053BankStatement
	@ManyToOne
	@JoinColumn(name = "BANK_STATEMENT_ID")
	@JsonIgnore
	private TipsCamt053BankStatement tipsCamt053BankStatement;

	//bi-directional many-to-one association to TipsCamt053BankAccountStatementEntry
	@OneToMany(mappedBy = "tipsCamt053BankAccountStatement")
	@JsonIgnore
	@ToString.Exclude
	private List<TipsCamt053BankAccountStatementEntry> tipsCamt053BankAccountStatementEntries;

}