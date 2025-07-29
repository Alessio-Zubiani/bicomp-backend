package it.popso.bicomp.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class CgsLacDetailDto {

	private BigDecimal lacId;
	private String lacNumber;
	private BigDecimal creditLtinAmount;
	private BigDecimal debitLtinAmount;
	private BigDecimal creditPmntAmount;
	private BigDecimal sctCreditPmntAmount;
	private BigDecimal corCreditPmntAmount;
	private BigDecimal b2bCreditPmntAmount;
	private BigDecimal debitPmntAmount;
	private BigDecimal sctDebitPmntAmount;
	private BigDecimal corDebitPmntAmount;
	private BigDecimal b2bDebitPmntAmount;
	private BigDecimal pendingDebitPmntAmount;
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