package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.UserDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.User;
import it.popso.bicomp.repository.UserRepository;
import it.popso.bicomp.service.impl.UserServiceImpl;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class UserServiceImplTest {
	
	@Mock
	private UserRepository repository;
	
	private UserServiceImpl service;
	
	
	@BeforeEach
    public void setup() {
		this.service = new UserServiceImpl(this.repository);
	}
	
	@Test
	@Order(1)
	void testFindAllUsers() {
		
		List<User> list = Instancio.ofList(createUserModel()).size(1).create();
		Mockito.when(this.repository.findAll()).thenReturn(list);
		
		List<UserDto> response = this.service.findAllUsers();
		assertThat(response).hasSize(1);
		assertThat(response.get(0).getRegistrationNumber()).isEqualTo(list.get(0).getRegistrationNumber());
	}
	
	@Test
	@Order(2)
	void testFindAllUsersNotFound() {
		List<User> list = Arrays.asList();
		Mockito.when(this.repository.findAll()).thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findAllUsers();
		});
	}
	
	@Test
	@Order(3)
	void testCreateUser() {
		
		User u = Instancio.create(createUserModel());
		UserDto dto = Instancio.create(createUserDtoModel());
		Mockito.when(this.repository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.save(Mockito.any(User.class))).thenReturn(u);
		
		BigDecimal id = this.service.createUser(dto);
		
		assertThat(id).isEqualByComparingTo(u.getId());
		
		verify(this.repository, times(1)).findByRegistrationNumber(Mockito.anyString());
		verify(this.repository, times(1)).save(Mockito.any(User.class));
	}
	
	@Test
	@Order(4)
	void testCreateUserNullField() {
		
		User u = Instancio.create(createUserModel());
		UserDto dto = Instancio.create(createUserDtoModel());
		Mockito.when(this.repository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.save(Mockito.any(User.class))).thenReturn(u);
		
		BigDecimal id = this.service.createUser(dto);
		
		assertThat(id).isEqualByComparingTo(u.getId());
		
		verify(this.repository, times(1)).findByRegistrationNumber(Mockito.anyString());
		verify(this.repository, times(1)).save(Mockito.any(User.class));
	}
	
	@Test
	@Order(5)
	void testCreateUserDataIntegrityViolationException() {
		
		UserDto dto = Instancio.create(createUserDtoModel());
		Mockito.when(this.repository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(this.repository.save(Mockito.any(User.class))).thenThrow(new DataIntegrityViolationException("USERS_REGISTRATION_NUMBER_IDX"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.createUser(dto);
		});
		
		verify(this.repository, times(1)).findByRegistrationNumber(Mockito.anyString());
		verify(this.repository, times(1)).save(Mockito.any(User.class));
	}
	
	@Test
	@Order(6)
	void testFindUserById() {
		
		User u = Instancio.create(createUserModel());
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(u));
		
		User user = this.service.findById(BigDecimal.ONE);
		
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo(u.getName());
	}
	
	@Test
	@Order(7)
	void testFindUserByIdNotFound() {		
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findById(BigDecimal.ONE);
		});
	}
	
	@Test
	@Order(8)
	void testFindUserByRegistrationNumber() {
		
		User u = Instancio.create(createUserModel());
		Mockito.when(this.repository.findByRegistrationNumber(Mockito.any(String.class))).thenReturn(Optional.of(u));
		
		User user = this.service.findByRegistrationNumber("1");
		
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo(u.getName());
	}
	
	@Test
	@Order(9)
	void testFindUserByRegistrationNumberNotFound() {
		
		Mockito.when(this.repository.findByRegistrationNumber(Mockito.any(String.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findByRegistrationNumber("1");
		});
	}
	
	@Test
	@Order(10)
	void testUpdateUser() {
		
		User u = Instancio.create(createUserModel());
		UserDto dto = Instancio.create(createUserDtoModel());
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(u));
		Mockito.when(this.repository.save(Mockito.any(User.class))).thenReturn(u);
		
		User user = this.service.updateUser(dto);
		
		assertThat(user).isNotNull();
		verify(this.repository, times(1)).save(Mockito.any(User.class));
	}
	
	@Test
	@Order(11)
	void testUpdateUserNotFound() {
		
		UserDto dto = Instancio.create(createUserDtoModel());
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.updateUser(dto);
		});
		verify(this.repository, times(0)).save(Mockito.any(User.class));
	}
	
	@Test
	@Order(12)
	void testDeleteUser() {
		
		User u = Instancio.create(createUserModel());
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(u));
		
		this.service.deleteUser(BigDecimal.ONE);
		
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.repository, times(1)).delete(Mockito.any(User.class));
	}
	
	@Test
	@Order(13)
	void testDeleteUserNotFound() {
		Mockito.when(this.repository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.deleteUser(BigDecimal.ONE);
		});
		verify(this.repository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.repository, times(0)).save(Mockito.any(User.class));
	}
	
	@Test
	@Order(14)
	void testUserServiceImplEquals() {
		
		UserServiceImpl test = new UserServiceImpl(this.repository);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(15)
	void testUserServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(16)
	void testUserServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(17)
	void testUserServiceImplEqualsNull() {
		
		this.service = new UserServiceImpl(null);
		UserServiceImpl test = new UserServiceImpl(null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(18)
	void testUserServiceImplEqualsOtherNotNull() {
		
		this.service = new UserServiceImpl(null);
		UserServiceImpl test = new UserServiceImpl(this.repository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(19)
	void testUserServiceImplNotEquals() {
		
		UserServiceImpl test = new UserServiceImpl(null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(20)
	void testUserServiceImplEqualsHashCode() {
		
		UserServiceImpl test = new UserServiceImpl(this.repository);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(21)
	void testUserServiceImplNotEqualsHashCode() {
		
		UserServiceImpl test = new UserServiceImpl(null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}
	
	@Test
	@Order(22)
	void testCreateUserBicompException() {
		
		UserDto dto = Instancio.create(createUserDtoModel());
		User u = Instancio.create(createUserModel());
		Mockito.when(this.repository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.of(u));
		Mockito.when(this.repository.save(Mockito.any(User.class))).thenThrow(new DataIntegrityViolationException("USERS_REGISTRATION_NUMBER_IDX"));
		
		assertThrows(BicompException.class, () -> {
			this.service.createUser(dto);
		});
		
		verify(this.repository, times(1)).findByRegistrationNumber(Mockito.anyString());
		verify(this.repository, times(0)).save(Mockito.any(User.class));
	}
	
	public static Model<User> createUserModel() {
		
		Model<User> model = Instancio.of(User.class)
				.generate(field(User::getId), gen -> gen.math().bigDecimal().scale(0))
				.set(field(User::getName), "name")
				.set(field(User::getSurname), "surname")
				.set(field(User::getEmail), "user@user.it")
				.toModel();
		
		return model;
	}
	
	public static Model<UserDto> createUserDtoModel() {
		
		Model<UserDto> model = Instancio.of(UserDto.class)
				.generate(field(UserDto::getId), gen -> gen.math().bigDecimal().scale(0))
				.set(field(UserDto::getName), "name")
				.set(field(UserDto::getSurname), "surname")
				.set(field(UserDto::getEmail), "user@user.it")
				.toModel();
		
		return model;
	}

}
