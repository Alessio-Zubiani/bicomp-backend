package it.popso.bicomp.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@MappedSuperclass

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsBulkReferenceEntity extends CgsBaseEntity {
	
    public CgsBulkReferenceEntity() {
		super();
	}

	public CgsBulkReferenceEntity(BigDecimal id, Date tmsInsert, String bulkReference, Date creationDateTime) {
        super(id, tmsInsert);
        this.creationDateTime = creationDateTime;
        this.bulkReference = bulkReference;
    }
	
	@Column(name = "BULK_REFERENCE")
	private String bulkReference;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE_TIME")
	private Date creationDateTime;

}
