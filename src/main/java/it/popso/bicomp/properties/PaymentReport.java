package it.popso.bicomp.properties;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReport {
	
	private String paymentReportFolder;
	private String paymentReportPrefix;
	private String paymentReportSuffix;
	
	@Override
	public int hashCode() {
		return Objects.hash(paymentReportFolder, paymentReportPrefix, paymentReportSuffix);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaymentReport other = (PaymentReport) obj;
		return Objects.equals(paymentReportFolder, other.paymentReportFolder)
				&& Objects.equals(paymentReportPrefix, other.paymentReportPrefix)
				&& Objects.equals(paymentReportSuffix, other.paymentReportSuffix);
	}

}
