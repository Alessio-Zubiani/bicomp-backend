package it.popso.bicomp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import it.popso.bicomp.filter.GlobalExceptionHandlerFilter;
import it.popso.bicomp.filter.SSOAuthorizationFilter;
import it.popso.bicomp.handler.AccessDeniedExceptionHandler;
import it.popso.bicomp.handler.AuthenticationExceptionHandler;
import it.popso.bicomp.repository.UserRepository;
import it.popso.sso.auth.filter.SSOAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
	
	private static final String USER = "user";
	private static final String ADMIN = "admin";
	
	private final UserRepository userRepository;
	private final AccessDeniedExceptionHandler accessDeniedExceptionHandler;
	private final AuthenticationExceptionHandler authenticationExceptionHandler;
	private final AuthenticationEntryPoint authenticationEntryPoint;
    
    
    @Bean
    public GlobalExceptionHandlerFilter globExceptionHandlerFilter() {
        return new GlobalExceptionHandlerFilter(this.authenticationEntryPoint);
    }
	
	@Bean
    public SSOAuthenticationFilter ssoAuthenticationFilter() {
        return new SSOAuthenticationFilter();
    }
    
    @Bean
    public SSOAuthorizationFilter ssoAuthorizationFilter() {
        return new SSOAuthorizationFilter(this.userRepository);
    }
    
    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        return http
        		.cors(Customizer.withDefaults())
        		.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(this.ssoAuthenticationFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(this.globExceptionHandlerFilter(), SSOAuthenticationFilter.class)
                .addFilterAfter(this.ssoAuthorizationFilter(), BasicAuthenticationFilter.class)
                .securityMatcher(new PortRequestMatcher(8080))
                .authorizeHttpRequests(authz -> authz
                		.requestMatchers("/BicompService_v1/**").permitAll()
						.requestMatchers(HttpMethod.POST, new String[] { "/api/v1/notifications/send" }).permitAll()
						.requestMatchers(HttpMethod.GET, new String[] { "/api/v1/feedbacks/**" }).hasAuthority(ADMIN)
						.requestMatchers(HttpMethod.POST, new String[] { "/api/v1/feedbacks/{:\\d+}" }).hasAuthority(ADMIN)
						.requestMatchers(HttpMethod.GET, new String[] { "/api/v1/users" }).hasAuthority(ADMIN)
						.requestMatchers(HttpMethod.GET, new String[] { "/api/v1/users/{registrationNumber}" }).hasAnyAuthority(USER, ADMIN)
						.requestMatchers(HttpMethod.POST, new String[] { "/api/v1/users/update" }).hasAnyAuthority(USER, ADMIN)
						.requestMatchers(HttpMethod.POST, new String[] { "/api/v1/users", "/api/v1/users/{:\\d+}" }).hasAuthority(ADMIN)
						.requestMatchers("/api/v1/timers/**", "/api/v1/files/**").hasAuthority(ADMIN)
						.requestMatchers("/api/v1/tips/**", "/api/v1/cgs/**", "/api/v1/compensazioneNazionale/**", "/api/v1/rt1/**").hasAnyAuthority(USER, ADMIN)
						.anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handle ->
                        handle.authenticationEntryPoint(this.authenticationExceptionHandler)
                                .accessDeniedHandler(this.accessDeniedExceptionHandler))
                .headers(header -> header
                				.xssProtection(Customizer.withDefaults())
                				.contentSecurityPolicy(contentSecurityPolicy -> 
                					contentSecurityPolicy.policyDirectives("script-src 'self'")
                				)
                		)
                .build();
    }
    
    @Bean
	@Order(2)
	public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
		
		return http
				.securityMatcher(new PortRequestMatcher(6556))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/actuator/**").permitAll()
				)
				.build();
	}

}
