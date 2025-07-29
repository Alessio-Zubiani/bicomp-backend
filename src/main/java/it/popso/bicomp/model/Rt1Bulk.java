package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;


/**
 * The persistent class for the RT1_BULK database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="RT1_BULK")
public class Rt1Bulk implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RT1_BULK_ID_GENERATOR", sequenceName="RT1_BULK_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RT1_BULK_ID_GENERATOR")
	private BigDecimal id;

	@Column(name="ENV_INDICATOR")
	private Character envIndicator;

	@Column(name="FILE_CYCLE")
	private String fileCycle;

	@Temporal(TemporalType.DATE)
	@Column(name="FILE_DATE")
	private Date fileDate;

	@Column(name="FILE_REFERENCE")
	private String fileReference;

	@Column(name="FILE_TYPE")
	private String fileType;

	@Column(name="RECEIVING_INSTITUTE")
	private String receivingInstitute;
	
	@Column(name="REPORT_NAME")
	private String reportName;

	@Column(name="SENDING_INSTITUTE")
	private String sendingInstitute;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;

	//bi-directional many-to-one association to Rt1BulkPayment
	@OneToMany(mappedBy="rt1Bulk")
	private List<Rt1BulkPayment> rt1BulkPayments;

}