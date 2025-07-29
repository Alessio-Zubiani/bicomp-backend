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
public class CgsBaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CGS_BASE_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "CGS_BASE_SEQUENCE_GENERATOR", sequenceName = "CGS_BASE_SEQUENCE", allocationSize = 1)
	private BigDecimal id;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_INSERT")
	private Date tmsInsert;

}
