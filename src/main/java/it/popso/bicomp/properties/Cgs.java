package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Cgs extends PaymentReport {
	
	private String share;
	private String prefix;
	private String suffix;
	
	@Override
	public int hashCode() {
		return Objects.hash(prefix, share, suffix);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cgs other = (Cgs) obj;
		return Objects.equals(prefix, other.prefix) && Objects.equals(share, other.share)
				&& Objects.equals(suffix, other.suffix);
	}
}
