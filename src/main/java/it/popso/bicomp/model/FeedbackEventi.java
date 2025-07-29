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
import java.util.Date;


/**
 * The persistent class for the FEEDBACK_EVENTI database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name="FEEDBACK_EVENTI")
public class FeedbackEventi implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "FEEDBACK_EVENTI_ID_GENERATOR", sequenceName = "FEEDBACK_EVENTI_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FEEDBACK_EVENTI_ID_GENERATOR")
	private BigDecimal id;

	@Column(name="DESCRIZIONE")
	private String descrizione;
	
	@Column(name="INSERT_USER")
	private BigDecimal insertUser;
	
	@Column(name="STATO")
	private String stato;

	@CreationTimestamp
	@Column(name="TMS_INSERT", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date tmsInsert;
	
	//bi-directional many-to-one association to CgsLmr
	@ManyToOne
	@JoinColumn(name="FEEDBACK_ID")
	@ToString.Exclude
	private Feedback feedback;

}