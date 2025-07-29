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
public class PsrParticipantBody {
	
	@FixedField(label = "Record Type", start = 0, length = 4, require = true)
	private String recordType;
	
	@FixedField(label = "Operation Type", start = 4, length = 4, require = true)
	private String operationType;
	
	@FixedField(label = "Liquidity Instruction Reference", start = 8, length = 35, require = true)
	private String liquidityInstructionReference;
	
	@FixedField(label = "Liquidity Operation Amount", start = 43, length = 18, require = true)
	private String liquidityOperationAmount;
	
	@FixedField(label = "Liquidity Instruction Status", start = 61, length = 3, require = true)
	private String liquidityInstructionStatus;

}