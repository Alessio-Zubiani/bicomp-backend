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
public class PsrParticipantHeader {
	
	@FixedField(label = "Record Type", start = 0, length = 4, require = true)
	private String recordType;
	
	@FixedField(label = "Participant BIC", start = 4, length = 8, require = true)
	private String settlementBic;
	
	@FixedField(label = "Participant Position Initial (Debit/Credit Indicator)", start = 12, length = 2, require = true)
	private String initialParticipantPositionIndicator;
	
	@FixedField(label = "Participant Position Initial", start = 14, length = 18, require = true)
	private String initialParticipantPosition;
	
	@FixedField(label = "Participant Position Final (Debit/Credit Indicator)", start = 32, length = 2, require = true)
	private String finalParticipantPositionIndicator;
	
	@FixedField(label = "Participant Position Final", start = 34, length = 18, require = true)
	private String finalParticipantPosition;
	
	@FixedField(label = "Received Funding Amount", start = 52, length = 18, require = true)
	private String receivedFundingAmount;
	
	@FixedField(label = "Requested Funding Amount", start = 70, length = 18, require = true)
	private String requestedFundingAmount;
	
	@FixedField(label = "Requested Defunding Amount", start = 88, length = 18, require = true)
	private String requestedDefundingAmount;
	
	@FixedField(label = "Rejected Defunding Amount", start = 106, length = 18, require = true)
	private String rejectedDefundingAmount;
	
	@FixedField(label = "Credit Liquidity Transfer Amount", start = 124, length = 18, require = true)
	private String creditLiquidityTransferAmount;
	
	@FixedField(label = "Debit Liquidity Transfer Amount", start = 142, length = 18, require = true)
	private String debitLiquidityTransferAmount;
	
	@FixedField(label = "Instant / PRR Amount Sent", start = 160, length = 18, require = true)
	private String instantPrrSentAmount;
	
	@FixedField(label = "Instant / PRR Amount Received", start = 178, length = 18, require = true)
	private String instantPrrReceivedAmount;
	
	@FixedField(label = "Instant / PRR Amount Pending", start = 196, length = 18, require = true)
	private String instantPrrPendingAmount;
	
	@FixedField(label = "Rejected Amount Previous LAC Pending Instant / PRR", start = 214, length = 18, require = true)
	private String rejectedAmountPreviousInstantPrrPending;
	
	@FixedField(label = "Accepted Amount Previous LAC Pending Instant / PRR", start = 232, length = 18, require = true)
	private String acceptedAmountPreviousInstantPrrPending;
	
	@FixedField(label = "Instant / PRR RT1A Amount Sent", start = 250, length = 18, require = true)
	private String instantPrrRt1aSentAmount;
	
	@FixedField(label = "Instant / PRR RT1A Amount Received", start = 268, length = 18, require = true)
	private String instantPrrRt1aReceivedAmount;
	
	@FixedField(label = "Instant / PRR RT1A Amount Pending", start = 286, length = 18, require = true)
	private String instantPrrRt1aPendingAmount;
	
	@FixedField(label = "Rejected Amount Previous LAC Pending Instant / PRR RT1A", start = 304, length = 18, require = true)
	private String rejectedAmountPreviousInstantPrrRt1aPending;
	
	@FixedField(label = "Accepted Amount Previous LAC Pending Instant / PRR RT1A", start = 322, length = 18, require = true)
	private String acceptedAmountPreviousInstantPrrRt1aPending;

}