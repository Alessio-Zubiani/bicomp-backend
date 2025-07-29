package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Access {
	
	private String name;
	private String secret;
	
	@Override
	public int hashCode() {
		return Objects.hash(name, secret);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Access other = (Access) obj;
		return Objects.equals(name, other.name) && Objects.equals(secret, other.secret);
	}

}
