package it.popso.bicomp.flat;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@FixedObject
public class PsrFileTrailer {
	
	@FixedField(label = "Record Type", start = 0, length = 4, require = true)
	private String recordType;

}
