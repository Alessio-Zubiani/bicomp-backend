package it.popso.bicomp.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import it.popso.bicomp.dto.UserDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;

public interface UserService {
	
	List<UserDto> findAllUsers() throws ResourceNotFoundException;
	
	BigDecimal createUser(UserDto dto) throws DataIntegrityViolationException;
	
	User findById(BigDecimal id) throws ResourceNotFoundException;
	
	User findByRegistrationNumber(String registrationNumber) throws ResourceNotFoundException;
	
	User updateUser(UserDto u) throws ResourceNotFoundException;
	
	void deleteUser(BigDecimal id) throws ResourceNotFoundException;

}
