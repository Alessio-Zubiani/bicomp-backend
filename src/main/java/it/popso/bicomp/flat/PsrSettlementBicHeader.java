package it.popso.bicomp.flat;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@FixedObject
public class PsrSettlementBicHeader {
	
	@FixedField(label = "Record Type", start = 0, length = 4, require = true)
	private String recordType;
	
	@FixedField(label = "Settlement BIC", start = 4, length = 11, require = true)
	private String settlementBic;
	
	@FixedField(label = "Liquidity Position Initial (Debit/Credit Indicator)", start = 15, length = 2, require = true)
	private String initialPositionIndicator;
	
	@FixedField(label = "Liquidity Position Initial", start = 17, length = 18, require = true)
	private String initialLiquidityPosition;
	
	@FixedField(label = "Liquidity Position Final (Debit/Credit Indicator)", start = 35, length = 2, require = true)
	private String finalPositionIndicator;
	
	@FixedField(label = "Liquidity Position Final", start = 37, length = 18, require = true)
	private String finalLiquidityPosition;

}