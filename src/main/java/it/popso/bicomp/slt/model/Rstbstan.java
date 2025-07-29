package it.popso.bicomp.slt.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;


/**
 * The persistent class for the RSTBSTAN database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "RSTBSTAN", schema = "SLT")
public class Rstbstan implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private RstbstanPK id;
	
	@Column(name="STAN_IMPO_DEF_AV")
	private BigDecimal stanImpoDefAv;

	@Column(name="STAN_IMPO_DEF_DA")
	private BigDecimal stanImpoDefDa;

	@Column(name="STAN_IMPO_PREV_AV")
	private BigDecimal stanImpoPrevAv;

	@Column(name="STAN_IMPO_PREV_DA")
	private BigDecimal stanImpoPrevDa;

	@Column(name="STAN_IMPO_PREV1_AV")
	private BigDecimal stanImpoPrev1Av;

	@Column(name="STAN_IMPO_PREV1_DA")
	private BigDecimal stanImpoPrev1Da;

	@Column(name="STAN_IMPO_PREV2_AV")
	private BigDecimal stanImpoPrev2Av;

	@Column(name="STAN_IMPO_PREV2_DA")
	private BigDecimal stanImpoPrev2Da;

	@Column(name="STAN_IMPO_PREV3_AV")
	private BigDecimal stanImpoPrev3Av;

	@Column(name="STAN_IMPO_PREV3_DA")
	private BigDecimal stanImpoPrev3Da;

	@Column(name="STAN_IMPO_PREV4_AV")
	private BigDecimal stanImpoPrev4Av;

	@Column(name="STAN_IMPO_PREV4_DA")
	private BigDecimal stanImpoPrev4Da;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="STAN_TIMESTAMP")
	private Date stanTimestamp;
	
}