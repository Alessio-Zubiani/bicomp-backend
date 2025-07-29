package it.popso.bicomp.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import it.popso.bicomp.properties.BicompConfig;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {
	
	private final BicompConfig config;

	@Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(this.config.getMinio().getUrl())
                .credentials(this.config.getMinio().getAccess().getName(), this.config.getMinio().getAccess().getSecret())
                .build();
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinioConfig other = (MinioConfig) obj;
		return Objects.equals(config, other.config);
	}

	@Override
	public int hashCode() {
		return Objects.hash(config);
	}
	
}
