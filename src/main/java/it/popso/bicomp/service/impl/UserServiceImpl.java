package it.popso.bicomp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.popso.bicomp.dto.UserDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;
import it.popso.bicomp.repository.UserRepository;
import it.popso.bicomp.service.UserService;
import it.popso.bicomp.utils.BicompConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository usersRepository;
	

	@Override
	public List<UserDto> findAllUsers() throws ResourceNotFoundException {
		
		log.info("Getting list of authorized users".replaceAll(BicompConstants.LOG_CRLF, ""));
		
		List<User> users = this.usersRepository.findAll();
		if(users.isEmpty()) {
			throw new ResourceNotFoundException("No users found");
		}
		
		log.info("List all users: {}".replaceAll(BicompConstants.LOG_CRLF, ""), Arrays.toString(users.toArray()));
		
		List<UserDto> dtos = new ArrayList<>();
		for(User u : users) {
			UserDto user = UserDto.builder()
					.id(u.getId())
					.registrationNumber(u.getRegistrationNumber())
					.name(u.getName())
					.surname(u.getSurname())
					.email(u.getEmail())
					.creationDateTime(u.getCreationDateTime())
					.lastLoginDateTime(u.getLastLoginDateTime())
					.build();
			dtos.add(user);
		}
		
		return dtos;
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = {DataIntegrityViolationException.class, BicompException.class}
	)
	public BigDecimal createUser(UserDto dto) throws DataIntegrityViolationException, BicompException {
		
		log.info("Creating user with name [{}]".replaceAll(BicompConstants.LOG_CRLF, ""), new StringBuilder(dto.getName()).append(" ").append(dto.getSurname()));
		
		Optional<User> o = this.usersRepository.findByRegistrationNumber(dto.getRegistrationNumber());
		if(o.isPresent()) {
			throw new BicompException(new StringBuilder("User with Registration Number [").append(dto.getRegistrationNumber()).append("] already exists").toString());
		}
		
		User user = User.builder()
				.registrationNumber(dto.getRegistrationNumber())
				.name(dto.getName())
				.surname(dto.getSurname())
				.email(dto.getEmail())
				.codiceAreaDipendenza(dto.getCodiceAreaDipendenza() != null ? dto.getCodiceAreaDipendenza() : "0001")
				.codiceDipendenza(dto.getCodiceDipendenza() != null ? dto.getCodiceDipendenza() : "0001")
				.phone(dto.getPhone() != null ? dto.getPhone() : null)
				.mobilePhone(dto.getMobilePhone() != null ? dto.getPhone() : null)
				.creationDateTime(new Date())
				.build();
		
		return this.usersRepository.save(user).getId();
	}

	@Override
	public User findById(BigDecimal id) throws ResourceNotFoundException {
		
		log.info("Retrieving user with ID: [{}]".replaceAll(BicompConstants.LOG_CRLF, ""), id);
		
		Optional<User> o = this.usersRepository.findById(id);
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("User with ID [").append(id).append("] not found").toString());
		}
		
		return o.get();
	}

	@Override
	public User findByRegistrationNumber(String registrationNumber) throws ResourceNotFoundException {
		log.info("Retrieving user with registrationNumber: [{}]".replaceAll(BicompConstants.LOG_CRLF, ""), registrationNumber);
		
		Optional<User> u = this.usersRepository.findByRegistrationNumber(registrationNumber);
		if(!u.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("User with RegistrationNumber [").append(registrationNumber).append("] not found").toString());
		}
		
		return u.get();
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = {ResourceNotFoundException.class}
	)
	public User updateUser(UserDto u) throws ResourceNotFoundException {
		Optional<User> o = this.usersRepository.findById(u.getId());
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("User with ID [").append(u.getId()).append("] not found").toString());
		}
		
		User user = o.get();
		user.setRegistrationNumber(u.getRegistrationNumber());
		user.setName(u.getName());
		user.setSurname(u.getSurname());
		user.setEmail(u.getEmail());
		user.setCodiceAreaDipendenza(u.getCodiceAreaDipendenza());
		user.setCodiceDipendenza(u.getCodiceDipendenza());
		user.setPhone(u.getPhone());
		user.setMobilePhone(u.getMobilePhone());
		
		return this.usersRepository.save(user);
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = {ResourceNotFoundException.class}
	)
	public void deleteUser(BigDecimal id) throws ResourceNotFoundException {
		
		log.info("Deleting user with ID: [{}]".replaceAll(BicompConstants.LOG_CRLF, ""), id);
		User u = this.findById(id);
		
		this.usersRepository.delete(u);
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.usersRepository == null) ? 0 : this.usersRepository.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof UserServiceImpl))
	        return false;
	    
	    UserServiceImpl other = (UserServiceImpl) obj;
	    return (this.usersRepository == null && other.usersRepository == null) 
	    		|| (this.usersRepository != null && this.usersRepository.equals(other.usersRepository));
	}
	
}
