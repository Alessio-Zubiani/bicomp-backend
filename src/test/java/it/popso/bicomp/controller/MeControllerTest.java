package it.popso.bicomp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.popso.bicomp.dto.MeDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;
import it.popso.bicomp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class MeControllerTest {
	
	@Mock
	private UserService userService;
	
	private MeController controller;
	private HttpServletRequest request;
	private Authentication authentication;
	private GrantedAuthority grantedAuthority;
	

	@BeforeEach
	public void setup() {
		this.controller = new MeController(this.userService);
		
		this.request = Mockito.mock(HttpServletRequest.class);
		this.authentication = Mockito.mock(Authentication.class);
		this.grantedAuthority = Mockito.mock(GrantedAuthority.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(this.request));
	}
	
	@Test
	@Order(1)
	void testCheckAuthentication() {
		
		User user = User.builder()
				.registrationNumber("1")
				.name("bicomp_users_name_1")
				.surname("bicomp_users_surname_1")
				.creationDateTime(new Date())
				.lastLoginDateTime(new Date())
				.codiceAreaDipendenza("1")
				.codiceDipendenza("1")
				.email("bicomp_user_1@test.it")
				.phone("111122223333")
				.mobilePhone("00000001")
				.build();
		Mockito.when(this.userService.findByRegistrationNumber(Mockito.anyString())).thenReturn(user);
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.when(this.grantedAuthority.getAuthority()).thenReturn("ROLE");
		
		MeDto result = (MeDto) this.controller.checkAuthentication(this.request, this.authentication).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getUser().getRegistrationNumber()).isEqualTo(user.getRegistrationNumber());
		assertThat(result.getUser().getRole()).isNotNull();
		
		verify(this.userService, times(1)).findByRegistrationNumber(Mockito.anyString());
	}
	
	@Test
	@Order(2)
	void testCheckAuthenticationException() {
		
		Mockito.when(this.userService.findByRegistrationNumber(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.when(this.grantedAuthority.getAuthority()).thenReturn("ROLE");
		
		assertThrows(BadCredentialsException.class, () -> {
			this.controller.checkAuthentication(this.request, this.authentication);
		});
		
		verify(this.userService, times(1)).findByRegistrationNumber(Mockito.anyString());
	}
	
}
