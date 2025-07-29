package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;


/**
 * The persistent class for the TIPS_CAMT_053_BANK_STATEMENT database table.
 * 
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name = "TIPS_CAMT_053_BANK_STATEMENT", uniqueConstraints = {
	@UniqueConstraint(name = "TIPS_CAMT_053_BANK_STATEMENT_MSG_ID_IDX", columnNames = {"MSG_ID"})
})

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class TipsCamt053BankStatement extends TipsBankEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Builder
	public TipsCamt053BankStatement(BigDecimal id, Date tmsInsert, Date creationDateTime, String msgId, 
			String reportName, List<TipsCamt053BankAccountStatement> tipsCamt053BankAccountStatements) {
		super(id, tmsInsert, creationDateTime);
		this.msgId = msgId;
		this.reportName = reportName;
		this.tipsCamt053BankAccountStatements = tipsCamt053BankAccountStatements;
		
	}

	@Column(name = "MSG_ID")
	private String msgId;

	@Column(name = "REPORT_NAME")
	private String reportName;

	//bi-directional many-to-one association to TipsCamt053BankAccountStatement
	@OneToMany(mappedBy = "tipsCamt053BankStatement")
	@JsonIgnore
	@ToString.Exclude
	private List<TipsCamt053BankAccountStatement> tipsCamt053BankAccountStatements;

}