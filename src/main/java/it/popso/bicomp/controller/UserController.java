package it.popso.bicomp.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.dto.UserDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;
import it.popso.bicomp.service.UserService;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	

	@GetMapping
	@BicompLogger
	public ResponseEntity<Response<List<UserDto>>> getAllUsers() throws ResourceNotFoundException {
		
		List<UserDto> dtos = this.userService.findAllUsers();
		
		return ResponseEntity.ok()
				.body(Response.<List<UserDto>>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(dtos)
					.message("Users retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/{id}")
	@BicompLogger
	public ResponseEntity<Response<UserDto>> getUserById(@PathVariable("id") int id) throws ResourceNotFoundException {
		
		User u = this.userService.findById(new BigDecimal(id));
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(Response.<UserDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(this.userToUserDto(u))
					.message(new StringBuilder("Successfully retrieved user with id [").append(id).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/rn/{registrationNumber}")
	@BicompLogger
	public ResponseEntity<Response<UserDto>> getUserByRegistrationNumber(@PathVariable("registrationNumber") String registrationNumber) throws ResourceNotFoundException {
		
		User u = this.userService.findByRegistrationNumber(registrationNumber);
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(Response.<UserDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(this.userToUserDto(u))
					.message(new StringBuilder("Successfully retrieved user with registrationNumber [").append(registrationNumber).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> createUser(@RequestBody UserDto u) throws BicompException {
		
		try {
			BigDecimal createdId = this.userService.createUser(u);
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Response.<BigDecimal>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(createdId)
						.message(new StringBuilder("Successfully created user with ID [").append(createdId).append("]").toString())
						.isSuccess(true)
						.status(HttpStatus.CREATED.name())
						.statusCode(HttpStatus.CREATED.value())
						.build()
			);
		}
		catch(DataIntegrityViolationException e) {
			throw new BicompException(e);
		}
	}
	
	@PostMapping("/update")
	@BicompLogger
	public ResponseEntity<Response<UserDto>> updateUser(@RequestBody UserDto u) throws BicompException {
		
		try {
			User user = this.userService.updateUser(u);
			
			return ResponseEntity.status(HttpStatus.OK)
					.body(Response.<UserDto>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(this.userToUserDto(user))
						.message(new StringBuilder("Successfully updated user with registrationNumber [").append(u.getRegistrationNumber()).append("]").toString())
						.isSuccess(true)
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(DataIntegrityViolationException e) {
			throw new BicompException(e);
		}
	}
	
	@PostMapping("/{id}")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> deleteUser(@PathVariable("id") BigDecimal id) throws ResourceNotFoundException {
		
		this.userService.deleteUser(id);
		
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(id)
					.message(new StringBuilder("Successfully deleted user with ID [").append(id).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	private UserDto userToUserDto(User u) {
		return UserDto.builder()
				.id(u.getId())
				.registrationNumber(u.getRegistrationNumber())
				.name(u.getName())
				.surname(u.getSurname())
				.codiceAreaDipendenza(u.getCodiceAreaDipendenza())
				.codiceDipendenza(u.getCodiceDipendenza())
				.email(u.getEmail())
				.phone(u.getPhone())
				.mobilePhone(u.getMobilePhone())
				.build();
	}
	
}
