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
public class CgsStatementEntryEntity extends CgsBaseEntity {
	
    public CgsStatementEntryEntity() {
		super();
	}

	public CgsStatementEntryEntity(BigDecimal id, Date tmsInsert, String additionalInfo, String currency, 
    		String entryReference, BigDecimal paymentAmount, Timestamp settlementDateTime, Character side, 
    		String status) {
        super(id, tmsInsert);
        this.additionalInfo = additionalInfo;
        this.currency = currency;
        this.entryReference = entryReference;
        this.paymentAmount = paymentAmount;
        this.settlementDateTime = settlementDateTime;
        this.side = side;
        this.status = status;
    }
    
    @Column(name = "ADDITIONAL_INFO")
	private String additionalInfo;

	@Column(name = "CURRENCY")
	private String currency;

	@Column(name = "ENTRY_REFERENCE")
	private String entryReference;

	@Column(name = "PAYMENT_AMOUNT")
	private BigDecimal paymentAmount;

	@Column(name = "SETTLEMENT_DATE_TIME")
	private Timestamp settlementDateTime;

	@Column(name = "SIDE")
	private Character side;

	@Column(name = "STATUS")
	private String status;

}
