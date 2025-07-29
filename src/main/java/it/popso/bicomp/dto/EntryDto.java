package it.popso.bicomp.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class EntryDto {
	
	private BigDecimal entryId;
	private String entryReference;
	private BigDecimal paymentAmount;
	private String currency;
	private Date settlementDateTime;
	private Character side;
	private String service;
	private String status;
	private String debitor;
	private String creditor;
	private String rejectReasonCode;
	private String rejectReasonDescription;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
