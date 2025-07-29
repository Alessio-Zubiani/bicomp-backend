package it.popso.bicomp.security;

import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PortRequestMatcher implements RequestMatcher {

	private final int port;
	
	@Override
	public boolean matches(HttpServletRequest request) {
		
		return request.getLocalPort() == this.port;
	}

}
