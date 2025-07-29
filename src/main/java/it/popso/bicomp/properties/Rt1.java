package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Rt1 {
	
	private String share;
	private String bulkPrefix;
	private String bulkSuffix;
	private String psrPrefix;
	private String psrSuffix;
	
	@Override
	public int hashCode() {
		return Objects.hash(bulkPrefix, bulkSuffix, psrPrefix, psrSuffix, share);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rt1 other = (Rt1) obj;
		return Objects.equals(bulkPrefix, other.bulkPrefix) && Objects.equals(bulkSuffix, other.bulkSuffix)
				&& Objects.equals(psrPrefix, other.psrPrefix) && Objects.equals(psrSuffix, other.psrSuffix)
				&& Objects.equals(share, other.share);
	}

}
