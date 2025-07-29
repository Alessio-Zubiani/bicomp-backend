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
 * The persistent class for the RT1_PSR_FILE_HEADER database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="RT1_PSR_FILE_HEADER")
public class Rt1PsrFileHeader implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RT1_PSR_FILE_HEADER_ID_GENERATOR", sequenceName="RT1_PSR_FILE_HEADER_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RT1_PSR_FILE_HEADER_ID_GENERATOR")
	private BigDecimal id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_TIME")
	private Date dateTime;

	@Column(name="FILE_TYPE")
	private String fileType;

	private String lac;

	@Column(name="RECEIVING_INSTITUTION")
	private String receivingInstitution;

	@Column(name="RECORD_NUMBER")
	private BigDecimal recordNumber;
	
	@Column(name="REPORT_NAME")
	private String reportName;

	@Column(name="SENDER_FILE_REFERENCE")
	private String senderFileReference;

	@Column(name="SENDING_INSTITUTION")
	private String sendingInstitution;

	@Column(name="SERVICE_IDENTIFIER")
	private String serviceIdentifier;

	@Temporal(TemporalType.DATE)
	@Column(name="SETTLEMENT_DATE")
	private Date settlementDate;

	@Column(name="TEST_CODE")
	private String testCode;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;

	//bi-directional one-to-one association to Rt1PsrSettlementBic
	@OneToOne(mappedBy = "rt1PsrFileHeader")
	@JsonIgnore
	@ToString.Exclude
	private Rt1PsrSettlementBic rt1PsrSettlementBic;

}