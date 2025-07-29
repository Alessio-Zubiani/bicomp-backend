package it.popso.bicomp.aspect;

import java.util.Objects;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {
	
	private final MdcFilter mdcFilter;
	
	
	@Bean
    public FilterRegistrationBean<MdcFilter> servletRegistrationBean() {
		
        final FilterRegistrationBean<MdcFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(this.mdcFilter);
        filterRegistrationBean.setOrder(2);
        
        return filterRegistrationBean;
    }

	@Override
	public int hashCode() {
		return Objects.hash(mdcFilter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeanConfig other = (BeanConfig) obj;
		return Objects.equals(mdcFilter, other.mdcFilter);
	}

}
