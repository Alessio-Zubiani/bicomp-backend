package it.popso.bicomp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class Rt1TotalDto {

	private LocalDate settlementDate;
	private BigDecimal creditLtAmount;
	private BigDecimal debitLtAmount;
	private BigDecimal creditPmntAmount;
	private BigDecimal debitPmntAmount;
	private BigDecimal rejectedCreditPmntAmount;
	private BigDecimal rejectedDebitPmntAmount;
	private String currency;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
}