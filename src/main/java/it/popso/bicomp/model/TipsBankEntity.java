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
public class TipsBankEntity extends TipsBaseEntity {
	
    public TipsBankEntity() {
		super();
	}

	public TipsBankEntity(BigDecimal id, Date tmsInsert, Date creationDateTime) {
        super(id, tmsInsert);
        this.creationDateTime = creationDateTime;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE_TIME")
	private Date creationDateTime;

}
