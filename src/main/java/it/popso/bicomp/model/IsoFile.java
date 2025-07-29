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
import java.util.List;

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
@Table(name="ISO_FILE")
public class IsoFile implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="ISO_FILE_GENERATOR", sequenceName="ISO_FILE_SEQUENCE", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ISO_FILE_GENERATOR")
	@Column(name="ID")
	private BigDecimal id;

	@Column(name="FILE_NAME")
	private String filaName;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TMS_INSERT")
	private Date tmsInsert;
	
	//bi-directional many-to-one association to CgsLmrPlcrBulk
	@OneToMany(mappedBy="isoFile")
	private List<IsoExternalCodeSet> isoExternalCodeSets;

}