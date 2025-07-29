package it.popso.bicomp.filter;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import it.popso.bicomp.model.User;
import it.popso.bicomp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RequiredArgsConstructor
public class SSOAuthorizationFilter extends OncePerRequestFilter {

	private final UserRepository userRepository;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(currentAuthentication != null && currentAuthentication.isAuthenticated()) {
			Integer principal = (Integer) currentAuthentication.getPrincipal();
			
			Optional<User> o = this.userRepository.findByRegistrationNumber(String.valueOf(principal));
			if(!o.isPresent()) {
				throw new UsernameNotFoundException(
						new StringBuilder("User with registration number [").append(principal).append("] not found").toString());
			}
			else {
				log.info("User found: [{}]", principal);
			}
			
			User user = o.get();			
			if(request.getRequestURL().toString().contains("/api/v1/me")) {
				log.info("Update last login timestamp for user: [{}]", principal);
				user.setLastLoginDateTime(new Date());
				this.userRepository.save(user);
			}
		}
		
	    filterChain.doFilter(request, response);
	}

}
