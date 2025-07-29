package it.popso.bicomp.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandlerFilter extends OncePerRequestFilter {
	
	private final AuthenticationEntryPoint authAuthenticationEntryPoint;

	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		try {
			filterChain.doFilter(request, response);
		}
		catch(ServletException e) {
			AuthenticationException wrapped = new AuthenticationServiceException("Servlet exception", e);
			this.authAuthenticationEntryPoint.commence(request, response, wrapped);			
		}
		catch(AuthenticationException e) {
			this.authAuthenticationEntryPoint.commence(request, response, e);
		}
	}
	
}
