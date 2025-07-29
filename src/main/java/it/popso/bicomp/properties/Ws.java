package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Ws {
	
	private String contabilitaUsername;
	private String contabilitaPassword;
	private String forecastUsername;
	private String forecastPassword;
	
	@Override
	public int hashCode() {
		return Objects.hash(contabilitaPassword, contabilitaUsername, forecastPassword, forecastUsername);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ws other = (Ws) obj;
		return Objects.equals(contabilitaPassword, other.contabilitaPassword)
				&& Objects.equals(contabilitaUsername, other.contabilitaUsername)
				&& Objects.equals(forecastPassword, other.forecastPassword)
				&& Objects.equals(forecastUsername, other.forecastUsername);
	}

}
