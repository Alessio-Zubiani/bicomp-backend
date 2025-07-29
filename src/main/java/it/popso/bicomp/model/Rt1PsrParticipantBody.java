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


/**
 * The persistent class for the RT1_PSR_PARTICIPANT_BODY database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="RT1_PSR_PARTICIPANT_BODY")
public class Rt1PsrParticipantBody implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RT1_PSR_PARTICIPANT_BODY_ID_GENERATOR", sequenceName="RT1_PSR_PARTICIPANT_BODY_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RT1_PSR_PARTICIPANT_BODY_ID_GENERATOR")
	private BigDecimal id;

	@Column(name="PAYMENT_AMOUNT")
	private BigDecimal paymentAmount;

	@Column(name="PAYMENT_REFERENCE")
	private String paymentReference;

	@Column(name="PAYMENT_STATUS")
	private String paymentStatus;

	@Column(name="OPERATION_TYPE")
	private String operationType;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;

	//bi-directional many-to-one association to Rt1PsrParticipantHeader
	@ManyToOne
	@JoinColumn(name="RT1_PSR_PARTICIPANT_HEADER_ID")
	@JsonIgnore
	private Rt1PsrParticipantHeader rt1PsrParticipantHeader;

}