package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor

@Component
@PropertySource(value = "file:/app/config/application.yaml", factory = YamlPropertySourceFactory.class)
//@PropertySource(value = "file:src/main/resources/application.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "bicomp")
public class BicompConfig extends HibernateBaseEntity {
	
	@Builder
	public BicompConfig(Datasource datasource, Hibernate hibernate) {
		super(datasource, hibernate);
	}
	
	private Cgs cgs;
	private String cnCodiceFamiglia;
	private Rt1 rt1;
	private Tips tips;
	private Ws ws;
	private Minio minio;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(cgs, cnCodiceFamiglia, minio, rt1, tips, ws);
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
		BicompConfig other = (BicompConfig) obj;
		return Objects.equals(cgs, other.cgs) && Objects.equals(cnCodiceFamiglia, other.cnCodiceFamiglia)
				&& Objects.equals(minio, other.minio) && Objects.equals(rt1, other.rt1)
				&& Objects.equals(tips, other.tips) && Objects.equals(ws, other.ws);
	}

}
