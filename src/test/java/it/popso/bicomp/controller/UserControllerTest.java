package it.popso.bicomp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.Response;
import it.popso.bicomp.dto.UserDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;
import it.popso.bicomp.service.UserService;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class UserControllerTest {
	
	@Mock
	private UserService userService;
	
	private UserController controller;
	

	@BeforeEach
	public void setup() {
		this.controller = new UserController(this.userService);
	}
	
	@Test
	@Order(1)
	void testGetAllUsers() {
		
		List<UserDto> list = Arrays.asList(UserDto.builder()
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
				.build(), UserDto.builder()
				.registrationNumber("2")
				.name("bicomp_users_name_2")
				.surname("bicomp_users_surname_2")
				.creationDateTime(new Date())
				.lastLoginDateTime(new Date())
				.codiceAreaDipendenza("2")
				.codiceDipendenza("2")
				.email("bicomp_user_2@test.it")
				.phone("111122223333")
				.mobilePhone("00000002")
				.build()
			);
		Mockito.when(this.userService.findAllUsers()).thenReturn(list);
		
		List<UserDto> result = (List<UserDto>) this.controller.getAllUsers().getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getRegistrationNumber()).isEqualTo(list.get(0).getRegistrationNumber());
		
		verify(this.userService, times(1)).findAllUsers();
	}
	
	@Test
	@Order(2)
	void testGetAllUsersNotFound() {
		
		Mockito.when(this.userService.findAllUsers()).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getAllUsers();
		});
		
		verify(this.userService, times(1)).findAllUsers();
	}
	
	@Test
	@Order(3)
	void testCreateUser() {
		
		UserDto u = UserDto.builder()
				.registrationNumber("1")
				.name("bicomp_users_name_1")
				.surname("bicomp_users_surname_1")
				.codiceAreaDipendenza("1")
				.codiceDipendenza("1")
				.email("bicomp_user_1@test.it")
				.phone("111122223333")
				.mobilePhone("00000001")
				.build();
		Mockito.when(this.userService.createUser(Mockito.any(UserDto.class))).thenReturn(BigDecimal.ONE);
		
		BigDecimal result = (BigDecimal) this.controller.createUser(u).getBody().getResponse();
		
		assertThat(result).isNotNull().isGreaterThan(BigDecimal.ZERO);
		
		verify(this.userService, times(1)).createUser(Mockito.any(UserDto.class));
	}
	
	@Test
	@Order(4)
	void testCreateUserBicompException() {
		
		UserDto u = UserDto.builder()
				.registrationNumber("1")
				.name("bicomp_users_name_1")
				.surname("bicomp_users_surname_1")
				.codiceAreaDipendenza("1")
				.codiceDipendenza("1")
				.email("bicomp_user_1@test.it")
				.phone("111122223333")
				.mobilePhone("00000001")
				.build();
		Mockito.when(this.userService.createUser(Mockito.any(UserDto.class))).thenThrow(DataIntegrityViolationException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.createUser(u);
		});
		
		verify(this.userService, times(1)).createUser(Mockito.any(UserDto.class));
	}
	
	@Test
	@Order(5)
	void testDeleteUser() {
		
		Mockito.doNothing().when(this.userService).deleteUser(Mockito.any(BigDecimal.class));
		
		ResponseEntity<Response<BigDecimal>> result = this.controller.deleteUser(BigDecimal.ONE);
		
		assertThat(result.getBody().getIsSuccess()).isTrue();
		assertThat(result.getBody().getResponse()).isEqualTo(BigDecimal.ONE);
		verify(this.userService, times(1)).deleteUser(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(6)
	void testDeleteUserResourceNotFoundException() {
		
		Mockito.doThrow(ResourceNotFoundException.class).when(this.userService).deleteUser(Mockito.any(BigDecimal.class));
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.deleteUser(BigDecimal.ONE);
		});
		
		verify(this.userService, times(1)).deleteUser(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(7)
	void testGetUserByRegistrationNumber() {
		
		User u = User.builder()
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
		Mockito.when(this.userService.findByRegistrationNumber(Mockito.anyString())).thenReturn(u);
		
		UserDto result = (UserDto) this.controller.getUserByRegistrationNumber("0001").getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getRegistrationNumber()).isEqualTo(u.getRegistrationNumber());
		
		verify(this.userService, times(1)).findByRegistrationNumber(Mockito.anyString());
	}
	
	@Test
	@Order(8)
	void testGetUserByRegistrationNumberNotFound() {
		
		Mockito.when(this.userService.findByRegistrationNumber(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getUserByRegistrationNumber("0001");
		});
		
		verify(this.userService, times(1)).findByRegistrationNumber(Mockito.anyString());
	}
	
	@Test
	@Order(9)
	void testUpdateUser() {
		
		UserDto dto = UserDto.builder()
				.registrationNumber("1")
				.name("bicomp_users_name_1")
				.surname("bicomp_users_surname_1")
				.codiceAreaDipendenza("1")
				.codiceDipendenza("1")
				.email("bicomp_user_1@test.it")
				.phone("111122223333")
				.mobilePhone("00000001")
				.build();
		User u = User.builder()
				.registrationNumber("1")
				.name("bicomp_users_name_1")
				.surname("bicomp_users_surname_1")
				.codiceAreaDipendenza("1")
				.codiceDipendenza("1")
				.email("bicomp_user_1@test.it")
				.phone("111122223333")
				.mobilePhone("00000001")
				.build();
		Mockito.when(this.userService.updateUser(Mockito.any(UserDto.class))).thenReturn(u);
		
		UserDto result = (UserDto) this.controller.updateUser(dto).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getRegistrationNumber()).isEqualTo(dto.getRegistrationNumber());
		assertThat(result.getMobilePhone()).isEqualTo(dto.getMobilePhone());
		
		verify(this.userService, times(1)).updateUser(Mockito.any(UserDto.class));
	}
	
	@Test
	@Order(10)
	void testUpdateUserBicompException() {
		
		UserDto u = UserDto.builder()
				.registrationNumber("1")
				.name("bicomp_users_name_1")
				.surname("bicomp_users_surname_1")
				.codiceAreaDipendenza("1")
				.codiceDipendenza("1")
				.email("bicomp_user_1@test.it")
				.phone("111122223333")
				.mobilePhone("00000001")
				.build();
		Mockito.when(this.userService.updateUser(Mockito.any(UserDto.class))).thenThrow(DataIntegrityViolationException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.updateUser(u);
		});
		
		verify(this.userService, times(1)).updateUser(Mockito.any(UserDto.class));
	}
	
	@Test
	@Order(11)
	void testGetUserById() {
		
		User u = User.builder()
				.id(BigDecimal.ONE)
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
		Mockito.when(this.userService.findById(Mockito.any(BigDecimal.class))).thenReturn(u);
		
		UserDto result = (UserDto) this.controller.getUserById(1).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(u.getId());
		
		verify(this.userService, times(1)).findById(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(12)
	void testGetUserByIdNotFound() {
		
		Mockito.when(this.userService.findById(Mockito.any(BigDecimal.class))).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getUserById(1);
		});
		
		verify(this.userService, times(1)).findById(Mockito.any(BigDecimal.class));
	}
	
}
