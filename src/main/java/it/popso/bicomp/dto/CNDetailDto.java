package it.popso.bicomp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class CNDetailDto {
	
	private String stanCycle;
	private String stanTipoMessaggio;
	private String stanTipoVoce;
	private String stanDescrizioneVoce;
	private LocalDate stanSettlementDate;
	private LocalDateTime stanSettlementDateTime;
	private BigDecimal stanSettledCreditAmount;
	private BigDecimal stanSettledDebitAmount;
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
