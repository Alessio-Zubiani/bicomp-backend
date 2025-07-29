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
 * The persistent class for the RT1_PSR_PARTICIPANT_HEADER database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="RT1_PSR_PARTICIPANT_HEADER")
public class Rt1PsrParticipantHeader implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RT1_PSR_PARTICIPANT_HEADER_ID_GENERATOR", sequenceName="RT1_PSR_PARTICIPANT_HEADER_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RT1_PSR_PARTICIPANT_HEADER_ID_GENERATOR")
	private BigDecimal id;

	@Column(name="ACCEPTED_PREV_PDNG_AMOUNT")
	private BigDecimal acceptedPrevPdngAmount;

	@Column(name="ACCEPTED_PREV_RT1A_PDNG_AMOUNT")
	private BigDecimal acceptedPrevRt1aPdngAmount;

	@Column(name="CREDIT_LT_AMOUNT")
	private BigDecimal creditLtAmount;

	@Column(name="DEBIT_LT_AMOUNT")
	private BigDecimal debitLtAmount;

	@Column(name="FINAL_PART_POSITION")
	private BigDecimal finalPartPosition;

	@Column(name="FINAL_PART_POSITION_INDICATOR")
	private String finalPartPositionIndicator;

	@Column(name="INITIAL_PART_POSITION")
	private BigDecimal initialPartPosition;

	@Column(name="INITIAL_PART_POSITION_INDICATOR")
	private String initialPartPositionIndicator;

	@Column(name="INSTANT_PRR_PENDING_AMOUNT")
	private BigDecimal instantPrrPendingAmount;

	@Column(name="INSTANT_PRR_RECEIVED_AMOUNT")
	private BigDecimal instantPrrReceivedAmount;

	@Column(name="INSTANT_PRR_RT1A_PENDING_AMOUNT")
	private BigDecimal instantPrrRt1aPendingAmount;

	@Column(name="INSTANT_PRR_RT1A_RECEIVED_AMOUNT")
	private BigDecimal instantPrrRt1aReceivedAmount;

	@Column(name="INSTANT_PRR_RT1A_SENT_AMOUNT")
	private BigDecimal instantPrrRt1aSentAmount;

	@Column(name="INSTANT_PRR_SENT_AMOUNT")
	private BigDecimal instantPrrSentAmount;

	@Column(name="RECEIVED_FUNDING_AMOUNT")
	private BigDecimal receivedFundingAmount;

	@Column(name="REJECTED_DEFUNDING_AMOUNT")
	private BigDecimal rejectedDefundingAmount;

	@Column(name="REJECTED_PREV_PDNG_AMOUNT")
	private BigDecimal rejectedPrevPdngAmount;

	@Column(name="REJECTED_PREV_RT1A_PDNG_AMOUNT")
	private BigDecimal rejectedPrevRt1aPdngAmount;

	@Column(name="REQUESTED_DEFUNDING_AMOUNT")
	private BigDecimal requestedDefundingAmount;

	@Column(name="REQUESTED_FUNDING_AMOUNT")
	private BigDecimal requestedFundingAmount;

	@Column(name="SETTLEMENT_BIC")
	private String settlementBic;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;

	//bi-directional many-to-one association to Rt1PsrParticipantBody
	@OneToMany(mappedBy="rt1PsrParticipantHeader")
	@JsonIgnore
	@ToString.Exclude
	private List<Rt1PsrParticipantBody> rt1PsrParticipantBodies;

	//bi-directional many-to-one association to Rt1PsrSettlementBic
	@ManyToOne
	@JoinColumn(name="RT1_PSR_SETTLEMENT_BIC_ID")
	@JsonIgnore
	private Rt1PsrSettlementBic rt1PsrSettlementBic;

}