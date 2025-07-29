package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.CreationTimestamp;

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
 * The persistent class for the RT1_PSR_SETTLEMENT_BIC database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="RT1_PSR_SETTLEMENT_BIC")
public class Rt1PsrSettlementBic implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RT1_PSR_SETTLEMENT_BIC_ID_GENERATOR", sequenceName="RT1_PSR_SETTLEMENT_BIC_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RT1_PSR_SETTLEMENT_BIC_ID_GENERATOR")
	private BigDecimal id;

	@Column(name="FINAL_LIQUIDITY_POSITION")
	private BigDecimal finalLiquidityPosition;

	@Column(name="FINAL_POSITION_INDICATOR")
	private String finalPositionIndicator;

	@Column(name="INITIAL_LIQUIDITY_POSITION")
	private BigDecimal initialLiquidityPosition;

	@Column(name="INITIAL_POSITION_INDICATOR")
	private String initialPositionIndicator;

	@Column(name="SETTLEMENT_BIC")
	private String settlementBic;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;

	//bi-directional many-to-one association to Rt1PsrParticipantHeader
	@OneToMany(mappedBy="rt1PsrSettlementBic")
	@JsonIgnore
	@ToString.Exclude
	private List<Rt1PsrParticipantHeader> rt1PsrParticipantHeaders;

	//bi-directional one-to-one association to Rt1PsrFileHeader
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "RT1_PSR_FILE_HEADER_ID", referencedColumnName = "ID")
	@JsonIgnore
	@ToString.Exclude
	private Rt1PsrFileHeader rt1PsrFileHeader;

}