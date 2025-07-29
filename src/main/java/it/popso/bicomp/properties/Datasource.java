package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Datasource {
	
	private String url;
	private String username;
	private String password;
	private String driver;
	
	@Override
	public int hashCode() {
		return Objects.hash(driver, password, url, username);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Datasource other = (Datasource) obj;
		return Objects.equals(driver, other.driver) && Objects.equals(password, other.password)
				&& Objects.equals(url, other.url) && Objects.equals(username, other.username);
	}

}
