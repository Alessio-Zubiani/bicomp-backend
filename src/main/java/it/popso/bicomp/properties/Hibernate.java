package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Hibernate {
	
    private String showSql;
    private String formatSql;
    
	@Override
	public int hashCode() {
		return Objects.hash(formatSql, showSql);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hibernate other = (Hibernate) obj;
		return Objects.equals(formatSql, other.formatSql) && Objects.equals(showSql, other.showSql);
	}

}
