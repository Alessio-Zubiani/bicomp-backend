package it.popso.bicomp.aspect;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.popso.bicomp.utils.BicompConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
public class MdcFilter extends OncePerRequestFilter {	

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		try {
			MDC.put(BicompConstants.MDC_UUID, UUID.randomUUID().toString());
			MDC.put(BicompConstants.MDC_ID_USER, request.getHeader(BicompConstants.SM_USER));
			MDC.put(BicompConstants.MDC_IP_ADDRESS, request.getRemoteAddr());
			MDC.put(BicompConstants.MDC_BANCA, "Banca Popolare di Sondrio Spa");
			MDC.put(BicompConstants.MDC_APPLICAZIONE, "Bicomp Backend");
			MDC.put(BicompConstants.MDC_NODO, request.getServerName());
			
            filterChain.doFilter(request, response);
        } 
		catch (IllegalArgumentException e) {
            log.error("Exception occurred in filter while setting attributes for logs: [{}]", e);
        } 
		finally {
			MDC.remove(BicompConstants.MDC_UUID);
			MDC.remove(BicompConstants.MDC_ID_USER);
			MDC.remove(BicompConstants.MDC_IP_ADDRESS);
			MDC.remove(BicompConstants.MDC_BANCA);
			MDC.remove(BicompConstants.MDC_APPLICAZIONE);
			MDC.remove(BicompConstants.MDC_NODO);
			MDC.remove(BicompConstants.MDC_ID_CONVERSAZIONE);
        }
	}

}
