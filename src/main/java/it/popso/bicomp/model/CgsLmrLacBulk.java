package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * The persistent class for the CGS_LMR_LAC_BULK database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_LAC_BULK")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrLacBulk extends CgsBulkReferenceEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
    public CgsLmrLacBulk(BigDecimal id, Date tmsInsert, String bulkReference, Date creationDateTime, 
    		CgsLmr cgsLmr, CgsLmrLacBulkStatement cgsLmrLacBulkStatement) {
        super(id, tmsInsert, bulkReference, creationDateTime);
        this.cgsLmr = cgsLmr;
        this.cgsLmrLacBulkStatement = cgsLmrLacBulkStatement;
    }

	//bi-directional one-to-one association to CgsLmr
	@OneToOne
	@JoinColumn(name = "LMR_ID")
	private CgsLmr cgsLmr;

	//bi-directional one-to-one association to CgsLmrLacBulkStatement
	@OneToOne(mappedBy = "cgsLmrLacBulk")
	private CgsLmrLacBulkStatement cgsLmrLacBulkStatement;

}