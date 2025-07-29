package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.*;

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
 * The persistent class for the CGS_LMR_PLCR_BULK database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR_PLCR_BULK")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmrPlcrBulk extends CgsBulkReferenceEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
    public CgsLmrPlcrBulk(BigDecimal id, Date tmsInsert, String bulkReference, Timestamp creationDateTime, 
    		CgsLmr cgsLmr, List<CgsLmrPlcrBulkStatement> cgsLmrPlcrBulkStatements) {
        super(id, tmsInsert, bulkReference, creationDateTime);
        this.cgsLmr = cgsLmr;
        this.cgsLmrPlcrBulkStatements = cgsLmrPlcrBulkStatements;
    }

	//bi-directional many-to-one association to CgsLmr
	@ManyToOne
	@JoinColumn(name = "LMR_ID")
	@ToString.Exclude
	private CgsLmr cgsLmr;

	//bi-directional many-to-one association to CgsLmrPlcrBulkStatement
	@OneToMany(mappedBy = "cgsLmrPlcrBulk")
	private List<CgsLmrPlcrBulkStatement> cgsLmrPlcrBulkStatements;

}