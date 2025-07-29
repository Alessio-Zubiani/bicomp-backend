package it.popso.bicomp.handler;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.popso.bicomp.dto.ErrorDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.utils.DateUtils;


@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(value = AuthenticationException.class)
	protected ResponseEntity<Response<ErrorDto>> handleAuthenticationException(AuthenticationException e, WebRequest request) {
		
		ErrorDto err = ErrorDto.builder()
				.errorMessage(ExceptionUtils.getRootCause(e).getMessage())
				.build();
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Response.<ErrorDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(err)
					.message(e.getMessage())
					.isSuccess(false)
					.status(HttpStatus.UNAUTHORIZED.name())
					.statusCode(HttpStatus.UNAUTHORIZED.value())
					.build());
	}
	
	@ExceptionHandler(value = AccessDeniedException.class)
	protected ResponseEntity<Response<ErrorDto>> handleAccessDeniedException(AccessDeniedException e, WebRequest request) {
		
		ErrorDto err = ErrorDto.builder()
				.errorMessage(ExceptionUtils.getRootCause(e).getMessage())
				.build();
		
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Response.<ErrorDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(err)
					.message(e.getMessage())
					.isSuccess(false)
					.status(HttpStatus.FORBIDDEN.name())
					.statusCode(HttpStatus.FORBIDDEN.value())
					.build());
	}
	
	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<Response<ErrorDto>> handleMultipartException(MultipartException e, WebRequest request) {
		
		ErrorDto err = ErrorDto.builder()
				.errorMessage(ExceptionUtils.getRootCause(e).getMessage())
				.build();
		
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(Response.<ErrorDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(err)
					.message(err.getErrorMessage())
					.isSuccess(false)
					.status(HttpStatus.PAYLOAD_TOO_LARGE.name())
					.statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
					.build());
	}
	
	@ExceptionHandler(value = FileManagerException.class)
	public ResponseEntity<Response<ErrorDto>> handleFileManagerException(FileManagerException e, WebRequest request) {
		
		ErrorDto err = ErrorDto.builder()
				.errorMessage(ExceptionUtils.getRootCause(e).getMessage())
				.build();
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Response.<ErrorDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(err)
					.message(err.getErrorMessage())
					.isSuccess(false)
					.status(HttpStatus.INTERNAL_SERVER_ERROR.name())
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.build()
		);
	}
	
	@ExceptionHandler(value = ResourceNotFoundException.class)
	public ResponseEntity<Response<ErrorDto>> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
		
		ErrorDto err = ErrorDto.builder()
				.errorMessage(ExceptionUtils.getRootCause(e).getMessage())
				.build();
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Response.<ErrorDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(err)
					.message(err.getErrorMessage())
					.isSuccess(false)
					.status(HttpStatus.NOT_FOUND.name())
					.statusCode(HttpStatus.NOT_FOUND.value())
					.build()
		);
	}
	
	@ExceptionHandler(value = BicompException.class)
	public ResponseEntity<Response<ErrorDto>> handleBicompException(BicompException e, WebRequest request) {
		
		ErrorDto err = ErrorDto.builder()
				.errorMessage(ExceptionUtils.getRootCause(e).getMessage())
				.build();
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Response.<ErrorDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(err)
					.message(err.getErrorMessage())
					.isSuccess(false)
					.status(HttpStatus.INTERNAL_SERVER_ERROR.name())
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.build()
		);
	}
	
}
