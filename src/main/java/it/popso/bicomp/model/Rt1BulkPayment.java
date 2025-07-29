package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

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
 * The persistent class for the RT1_BULK_PAYMENT database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="RT1_BULK_PAYMENT")
public class Rt1BulkPayment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RT1_BULK_PAYMENT_ID_GENERATOR", sequenceName="RT1_BULK_PAYMENT_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RT1_BULK_PAYMENT_ID_GENERATOR")
	private BigDecimal id;

	//@Temporal(TemporalType.TIMESTAMP)
	@Column(name="ACCEPTANCE_DATE_TIME")
	private Timestamp acceptanceDateTime;

	//@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE_TIME")
	private Timestamp creationDateTime;

	private String creditor;
	
	private String currency;

	private String debitor;

	@Column(name="MSG_ID")
	private String msgId;

	@Column(name="ORIGINAL_AMOUNT")
	private BigDecimal originalAmount;

	@Column(name="ORIGINAL_END_TO_END_ID")
	private String originalEndToEndId;

	@Column(name="ORIGINAL_INSTRUCTION_ID")
	private String originalInstructionId;

	@Column(name="ORIGINAL_MSG_ID")
	private String originalMsgId;

	@Column(name="ORIGINAL_MSG_NAME")
	private String originalMsgName;

	@Column(name="ORIGINAL_TRANSACTION_ID")
	private String originalTransactionId;

	@Column(name="ORIGINAL_TRANSACTION_NUMBER")
	private BigDecimal originalTransactionNumber;

	@Column(name="PAYMENT_AMOUNT")
	private BigDecimal paymentAmount;

	@Column(name="REJECT_REASON")
	private String rejectReason;

	@Temporal(TemporalType.DATE)
	@Column(name="SETTLEMENT_DATE")
	private Date settlementDate;
	
	private Character side;

	private String status;

	@Column(name="STATUS_ID")
	private String statusId;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;

	//bi-directional many-to-one association to Rt1Bulk
	@ManyToOne
	@JoinColumn(name="RT1_BULK_ID")
	@ToString.Exclude
	private Rt1Bulk rt1Bulk;

}