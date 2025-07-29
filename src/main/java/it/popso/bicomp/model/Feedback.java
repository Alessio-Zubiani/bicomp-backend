package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
 * The persistent class for the FEEDBACK database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "FEEDBACK")
public class Feedback implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "FEEDBACK_ID_GENERATOR", sequenceName = "FEEDBACK_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="FEEDBACK_ID_GENERATOR")
	private BigDecimal id;

	private String descrizione;

	@Column(name = "INSERT_USER")
	private BigDecimal insertUser;
	
	@Column(name = "UPDATE_USER")
	private BigDecimal updateUser;

	private String stato;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_INSERT", updatable = false)
	private Date tmsInsert;
	
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_UPDATE", insertable = false)
	private Date tmsUpdate;
	
	//bi-directional many-to-one association to CgsLmrPlcrBulk
	@OneToMany(mappedBy="feedback")
	private List<FeedbackEventi> feedbackEventis;

}