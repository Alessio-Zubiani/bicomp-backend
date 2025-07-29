package it.popso.bicomp.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

@MappedSuperclass
public class TipsBaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TIPS_BASE_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "TIPS_BASE_SEQUENCE_GENERATOR", sequenceName = "TIPS_BASE_SEQUENCE", allocationSize = 1)
	private BigDecimal id;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_INSERT")
	private Date tmsInsert;

}
