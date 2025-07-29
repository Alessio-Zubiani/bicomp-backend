package it.popso.bicomp.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter

@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class CgsLacDto extends LacReportDto {

	private BigDecimal lacId;
	private String lacNumber;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
}