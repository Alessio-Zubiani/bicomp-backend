package it.popso.bicomp;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;


@SpringBootApplication
@ComponentScan(basePackages = { "it.popso.bicomp*", "it.popso.sso.*" })
public class BicompWebApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BicompWebApplication.class, args);
	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(8));
        factory.setMaxRequestSize(DataSize.ofMegabytes(8));
        
        return factory.createMultipartConfig();
    }
	
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
}
