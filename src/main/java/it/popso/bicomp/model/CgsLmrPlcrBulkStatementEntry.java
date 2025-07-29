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
 * The persistent class for the CGS_LMR_PLCR_BULK_STATEMENT_ENTRY database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_PLCR_BULK_STATEMENT_ENTRY")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrPlcrBulkStatementEntry extends CgsStatementEntryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Builder
	public CgsLmrPlcrBulkStatementEntry(BigDecimal id, Date tmsInsert, String additionalInfo, String creditorBic, 
			String currency, String debitorBic, String entryReference, BigDecimal paymentAmount, Timestamp settlementDateTime, 
			Character side, String status, CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement) {
		super(id, tmsInsert, additionalInfo, currency, entryReference, paymentAmount, settlementDateTime, side, status);
		this.creditorBic = creditorBic;
		this.debitorBic = debitorBic;
		this.cgsLmrPlcrBulkStatement = cgsLmrPlcrBulkStatement;
	}

	@Column(name = "CREDITOR_BIC")
	private String creditorBic;

	@Column(name = "DEBITOR_BIC")
	private String debitorBic;

	//bi-directional many-to-one association to CgsLmrPlcrBulkStatement
	@ManyToOne
	@JoinColumn(name = "LMR_PLCR_BULK_STATEMENT_ID")
	@ToString.Exclude
	private CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement;

}