package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;


/**
 * The persistent class for the TIMER database table.
 * 
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name="ISO_EXTERNAL_CODE_SET")
public class IsoExternalCodeSet implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="ISO_EXTERNAL_CODE_SET_GENERATOR", sequenceName="ISO_EXTERNAL_CODE_SET_SEQUENCE", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ISO_EXTERNAL_CODE_SET_GENERATOR")
	@Column(name="ID")
	private BigDecimal id;

	@Column(name="CODE_DESCRIPTION")
	private String codeDescription;

	@Column(name="CODE_NAME")
	private String codeName;

	@Column(name = "CODE_VALUE")
	private String codeValue;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;
	
	//bi-directional many-to-one association to CgsLmr
	@ManyToOne
	@JoinColumn(name = "ISO_FILE_ID")
	@ToString.Exclude
	private IsoFile isoFile;

}