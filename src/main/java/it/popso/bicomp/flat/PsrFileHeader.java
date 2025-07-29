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
public class PsrFileHeader {
	
	@FixedField(label = "Record Type", start = 0, length = 4, require = true)
	private String recordType;
	
	@FixedField(label = "Service Identifier", start = 4, length = 3, require = true)
	private String serviceIdentifier;
	
	@FixedField(label = "File Type", start = 7, length = 3, require = true)
	private String fileType;
	
	@FixedField(label = "Sending Institution", start = 10, length = 8, require = true)
	private String sendingInstitution;
	
	@FixedField(label = "Sender File Reference", start = 18, length = 16, require = true)
	private String senderFileReference;
	
	@FixedField(label = "Date Time", start = 34, length = 12, require = true)
	private String dateTime;
	
	@FixedField(label = "Test Code", start = 46, length = 1, require = true)
	private String testCode;
	
	@FixedField(label = "Receiving Institution", start = 47, length = 8, require = true)
	private String receivingInstitution;
	
	@FixedField(label = "Settlement Date", start = 55, length = 6, require = true)
	private String settlementDate;
	
	@FixedField(label = "LAC", start = 61, length = 2, require = true)
	private String lac;
	
	@FixedField(label = "Total Number of Records", start = 63, length = 6, require = true)
	private String numberOfRecord;

}