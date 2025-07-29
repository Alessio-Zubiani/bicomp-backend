package it.popso.bicomp.slt.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
public class RstbstanPK implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Temporal(TemporalType.DATE)
	@Column(name="STAN_DATA")
	private Date stanData;
	
	@Column(name="STAN_ISTITUTO")
	private String stanIstituto;
	
	@Column(name="STAN_MESSAGGIO")
	private String stanMessaggio;

	@Column(name="STAN_NUMERO_CICLI")
	private BigDecimal stanNumeroCicli;
	
	@Column(name="STAN_TIPO_RETE")
	private String stanTipoRete;

	@Column(name="STAN_TIPO_VOCE")
	private String stanTipoVoce;

	@Override
	public int hashCode() {
		return Objects.hash(stanData, stanIstituto, stanMessaggio, stanNumeroCicli, stanTipoRete, stanTipoVoce);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RstbstanPK other = (RstbstanPK) obj;
		return Objects.equals(stanData, other.stanData) && Objects.equals(stanIstituto, other.stanIstituto)
				&& Objects.equals(stanMessaggio, other.stanMessaggio)
				&& Objects.equals(stanNumeroCicli, other.stanNumeroCicli)
				&& Objects.equals(stanTipoRete, other.stanTipoRete) && Objects.equals(stanTipoVoce, other.stanTipoVoce);
	}
	
}