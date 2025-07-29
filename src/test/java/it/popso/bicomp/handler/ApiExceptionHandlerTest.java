package it.popso.bicomp.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;

import it.popso.bicomp.dto.ErrorDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.exception.ResourceNotFoundException;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class ApiExceptionHandlerTest {
	
	private ApiExceptionHandler handler;
	private WebRequest request;
	

	@BeforeEach
	public void setup() {
		this.handler = new ApiExceptionHandler();
		this.request = Mockito.mock(WebRequest.class);
	}
	
	@Test
	@Order(1)
	void testHandleAuthenticationException() {
		
		AuthenticationException authenticationException = new AuthenticationException("Test authentication exception") {
			private static final long serialVersionUID = 1L;
		};
		
		ResponseEntity<Response<ErrorDto>> response = this.handler.handleAuthenticationException(authenticationException, request);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody().getMessage()).isEqualTo(authenticationException.getMessage());
		assertThat(response.getBody().getResponse()).isInstanceOf(ErrorDto.class);
	}
	
	@Test
	@Order(2)
	void testHandleAccessDeniedException() {
		
		AccessDeniedException accessDeniedException = new AccessDeniedException("Test access denied exception");
		
		ResponseEntity<Response<ErrorDto>> response = this.handler.handleAccessDeniedException(accessDeniedException, request);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody().getMessage()).isEqualTo(accessDeniedException.getMessage());
		assertThat(response.getBody().getResponse()).isInstanceOf(ErrorDto.class);
	}
	
	@Test
	@Order(3)
	void testHandleMultipartException() {
		
		MultipartException multipartException = new MultipartException("Test multipart exception");
		
		ResponseEntity<Response<ErrorDto>> response = this.handler.handleMultipartException(multipartException, request);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
		assertThat(response.getBody().getMessage()).isEqualTo(multipartException.getMessage());
		assertThat(response.getBody().getResponse()).isInstanceOf(ErrorDto.class);
	}
	
	@Test
	@Order(4)
	void testHandleFileManagerException() {
		
		FileManagerException fileManagerException = new FileManagerException("Test filemanager exception");
		
		ResponseEntity<Response<ErrorDto>> response = this.handler.handleFileManagerException(fileManagerException, request);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getMessage()).isEqualTo(fileManagerException.getMessage());
		assertThat(response.getBody().getResponse()).isInstanceOf(ErrorDto.class);
	}
	
	@Test
	@Order(5)
	void testHandleResourceNotFoundException() {
		
		ResourceNotFoundException fileManagerException = new ResourceNotFoundException("Test resource not found exception");
		
		ResponseEntity<Response<ErrorDto>> response = this.handler.handleResourceNotFoundException(fileManagerException, request);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().getMessage()).isEqualTo(fileManagerException.getMessage());
		assertThat(response.getBody().getResponse()).isInstanceOf(ErrorDto.class);
	}
	
	@Test
	@Order(5)
	void testHandleBicompException() {
		
		BicompException bicompException = new BicompException("Test bicomp exception");
		
		ResponseEntity<Response<ErrorDto>> response = this.handler.handleBicompException(bicompException, request);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getMessage()).isEqualTo(bicompException.getMessage());
		assertThat(response.getBody().getResponse()).isInstanceOf(ErrorDto.class);
	}

}
