package it.popso.bicomp.slt.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The primary key class for the EUR_COMMON_CONTO_REGO_CONTANTE database table.
 * 
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Embeddable
public class RstbprocPK implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name="PROC_TIPO_VOCE")
	private String procTipoVoce;

	@Override
	public int hashCode() {
		return Objects.hash(procTipoVoce);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RstbprocPK other = (RstbprocPK) obj;
		return Objects.equals(procTipoVoce, other.procTipoVoce);
	}
	
}