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
public class HibernateBaseEntity {
	
	private Datasource datasource;
	private Hibernate hibernate;
	
	@Override
	public int hashCode() {
		return Objects.hash(datasource, hibernate);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HibernateBaseEntity other = (HibernateBaseEntity) obj;
		return Objects.equals(datasource, other.datasource) && Objects.equals(hibernate, other.hibernate);
	}

}
