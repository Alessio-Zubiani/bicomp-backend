package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Tips extends PaymentReport {
	
	private String camt052Share;
	private String camt052Prefix;
	private String camt052Suffix;
	private String camt053Share;
	private String camt053Prefix;
	private String camt053Suffix;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(camt052Prefix, camt052Share, camt052Suffix, camt053Prefix, camt053Share, camt053Suffix);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tips other = (Tips) obj;
		return Objects.equals(camt052Prefix, other.camt052Prefix) && Objects.equals(camt052Share, other.camt052Share)
				&& Objects.equals(camt052Suffix, other.camt052Suffix)
				&& Objects.equals(camt053Prefix, other.camt053Prefix)
				&& Objects.equals(camt053Share, other.camt053Share)
				&& Objects.equals(camt053Suffix, other.camt053Suffix);
	}

}
