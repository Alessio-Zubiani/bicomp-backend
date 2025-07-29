package it.popso.bicomp.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@MappedSuperclass

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class CgsStatementEntity extends CgsBaseEntity {
	
    public CgsStatementEntity() {
		super();
	}

	public CgsStatementEntity(BigDecimal id, Date tmsInsert, String accountOwner, Timestamp creationDateTime, 
    		Timestamp fromDateTime, Date settlementDate, String statementReference, Timestamp toDateTime) {
        super(id, tmsInsert);
        this.accountOwner = accountOwner;
        this.creationDateTime = creationDateTime;
        this.fromDateTime = fromDateTime;
        this.settlementDate = settlementDate;
        this.statementReference = statementReference;
        this.toDateTime = toDateTime;
    }
    
    @Column(name = "ACCOUNT_OWNER")
	private String accountOwner;
    
    @Column(name = "CREATION_DATE_TIME")
	private Timestamp creationDateTime;

	@Column(name = "FROM_DATE_TIME")
	private Timestamp fromDateTime;

	@Temporal(TemporalType.DATE)
	@Column(name = "SETTLEMENT_DATE")
	private Date settlementDate;

	@Column(name = "STATEMENT_REFERENCE")
	private String statementReference;

	@Column(name = "TO_DATE_TIME")
	private Timestamp toDateTime;

}
