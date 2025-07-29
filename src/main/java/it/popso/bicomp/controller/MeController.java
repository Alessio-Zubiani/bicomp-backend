package it.popso.bicomp.controller;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.MeDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.dto.UserDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;
import it.popso.bicomp.service.UserService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {
	
	private final UserService userService;

	
	@PostMapping
	@BicompLogger
	public ResponseEntity<Response<MeDto>> checkAuthentication(HttpServletRequest request, Authentication authentication) throws UsernameNotFoundException {
		
		Integer principal = (Integer) authentication.getPrincipal();
		
		try {
			User user = this.userService.findByRegistrationNumber(String.valueOf(principal));
			log.debug(new StringBuilder("User found: [").append(principal).append("]").toString().replaceAll(BicompConstants.LOG_CRLF, ""));
			
			MeDto auth = MeDto.builder()
					.user(UserDto.builder()
							.name(user.getName())
							.surname(user.getSurname())
							.registrationNumber(user.getRegistrationNumber())
							.role(this.clearRole(Arrays.toString(authentication.getAuthorities().toArray())))
							.build())
					.build();
			
			return ResponseEntity.ok(Response.<MeDto>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(auth)
						.message("User authenticated with SiteMinder SSO")
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
					.build());
		}
		catch(ResourceNotFoundException e) {
			throw new BadCredentialsException(new StringBuilder("User with registration number [").append(principal).append("] not found").toString());
		}
	}
	
	private String clearRole(String role) {
		return role.replaceAll("\\[|\\]", "").replace("ROLE_", "");
	}

}
