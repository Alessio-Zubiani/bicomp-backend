package it.popso.bicomp.slt.model;

import java.io.Serializable;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the RSTBPROC database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "RSTBPROC", schema = "SLT")
public class Rstbproc implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private RstbprocPK id;
	
	@Column(name="PROC_DESCRIZIONE")
	private String procDescrizione;

	@Column(name="PROC_ISTITUTO")
	private String procIstituto;

}