package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the CGS_LMR_LTIN_BULK database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_LTIN_BULK")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrLtinBulk extends CgsBulkReferenceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Builder
    public CgsLmrLtinBulk(BigDecimal id, Date tmsInsert, String bulkReference, Timestamp creationDateTime, 
    		CgsLmr cgsLmr, CgsLmrLtinBulkStatement cgsLmrLtinBulkStatement) {
        super(id, tmsInsert, bulkReference, creationDateTime);
        this.cgsLmr = cgsLmr;
        this.cgsLmrLtinBulkStatement = cgsLmrLtinBulkStatement;
    }

	//bi-directional one-to-one association to CgsLmr
	@OneToOne
	@JoinColumn(name = "LMR_ID")
	@ToString.Exclude
	private CgsLmr cgsLmr;

	//bi-directional one-to-one association to CgsLmrLtinBulkStatement
	@OneToOne(mappedBy = "cgsLmrLtinBulk")
	private CgsLmrLtinBulkStatement cgsLmrLtinBulkStatement;

}