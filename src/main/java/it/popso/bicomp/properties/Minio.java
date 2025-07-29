package it.popso.bicomp.properties;

import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
public class Minio {
	
	private String url;
	private Access access;
	private Bucket bucket;
	
	@Override
	public int hashCode() {
		return Objects.hash(access, bucket, url);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Minio other = (Minio) obj;
		return Objects.equals(access, other.access) && Objects.equals(bucket, other.bucket)
				&& Objects.equals(url, other.url);
	}

}
