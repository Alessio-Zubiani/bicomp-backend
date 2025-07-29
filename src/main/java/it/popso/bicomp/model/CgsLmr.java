package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * The persistent class for the CGS_LMR database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "CGS_LMR")

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsLmr extends CgsBaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Builder
	public CgsLmr(BigDecimal id, Date tmsInsert, String environment, Timestamp fileCreationDateTime, String fileLac, String fileRef,
			Date fileSettlementDate, String fileType, String receivingInstitute, String reportName,
			String sendingInstitute, String serviceId, String settlementBic, CgsLmrLacBulk cgsLmrLacBulk,
			CgsLmrLtinBulk cgsLmrLtinBulk, List<CgsLmrPlcrBulk> cgsLmrPlcrBulks) {
		super(id, tmsInsert);
		this.environment = environment;
		this.fileCreationDateTime = fileCreationDateTime;
		this.fileLac = fileLac;
		this.fileRef = fileRef;
		this.fileSettlementDate = fileSettlementDate;
		this.fileType = fileType;
		this.receivingInstitute = receivingInstitute;
		this.reportName = reportName;
		this.sendingInstitute = sendingInstitute;
		this.serviceId = serviceId;
		this.settlementBic = settlementBic;
		this.cgsLmrLacBulk = cgsLmrLacBulk;
		this.cgsLmrLtinBulk = cgsLmrLtinBulk;
		this.cgsLmrPlcrBulks = cgsLmrPlcrBulks;
	}

	@Column(name = "ENVIRONMENT")
	private String environment;

	@Column(name = "FILE_CREATION_DATE_TIME")
	private Timestamp fileCreationDateTime;

	@Column(name = "FILE_LAC")
	private String fileLac;

	@Column(name = "FILE_REF")
	private String fileRef;

	@Temporal(TemporalType.DATE)
	@Column(name = "FILE_SETTLEMENT_DATE")
	private Date fileSettlementDate;

	@Column(name = "FILE_TYPE")
	private String fileType;

	@Column(name = "RECEIVING_INSTITUTE")
	private String receivingInstitute;

	@Column(name = "REPORT_NAME")
	private String reportName;

	@Column(name = "SENDING_INSTITUTE")
	private String sendingInstitute;

	@Column(name = "SERVICE_ID")
	private String serviceId;

	@Column(name = "SETTLEMENT_BIC")
	private String settlementBic;

	//bi-directional one-to-one association to CgsLmrLacBulk
	@OneToOne(mappedBy = "cgsLmr")
	private CgsLmrLacBulk cgsLmrLacBulk;

	//bi-directional one-to-one association to CgsLmrLtinBulk
	@OneToOne(mappedBy = "cgsLmr")
	private CgsLmrLtinBulk cgsLmrLtinBulk;

	//bi-directional many-to-one association to CgsLmrPlcrBulk
	@OneToMany(mappedBy="cgsLmr")
	private List<CgsLmrPlcrBulk> cgsLmrPlcrBulks;
	
}