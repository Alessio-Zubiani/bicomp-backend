package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.popso.bicomp.dto.NotificationDto;
import it.popso.bicomp.dto.NotificationEventDto;
import it.popso.bicomp.dto.PageableNotificationDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Notification;
import it.popso.bicomp.model.NotificationUser;
import it.popso.bicomp.repository.NotificationRepository;
import it.popso.bicomp.repository.NotificationUserRepository;
import it.popso.bicomp.repository.UserRepository;
import it.popso.bicomp.service.impl.NotificationServiceImpl;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class NotificationServiceImplTest {
	
	@Mock
	private NotificationRepository notificationRepository;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private NotificationUserRepository notificationUserRepository;
	
	@Mock
	private EmitterService emitterService;
	
	@Mock
	private SseEmitter sseEmitter;
	
	private NotificationServiceImpl service;
	
	
	@BeforeEach
    public void setup() {
		this.service = new NotificationServiceImpl(this.notificationRepository, this.userRepository, 
				this.notificationUserRepository, this.emitterService);
	}
	
	@Test
	@Order(1)
	void testCreateNotification() throws IOException {
		
		Notification n = Notification.builder()
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.build();
		Mockito.when(this.notificationRepository.save(Mockito.any(Notification.class))).thenReturn(n);
		Mockito.doNothing().when(this.emitterService).sendEvent(Mockito.any(Notification.class));
		
		this.service.createNotification("BICOMP", "Test", "Test Notification", "INFO");
		
		verify(this.notificationRepository, times(1)).save(Mockito.any(Notification.class));
		verify(this.emitterService, times(1)).sendEvent(Mockito.any(Notification.class));
	}
	
	@Test
	@Order(2)
	void testFindAllNotification() throws IOException {
		
		Page<Notification> p = new PageImpl<>(Arrays.asList(Notification.builder()
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.build()));
		Mockito.when(this.notificationRepository.findAll(Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableNotificationDto result = this.service.findAllNotification(PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getMessage()).isEqualTo(p.getContent().get(0).getMessage());
		
		verify(this.notificationRepository, times(1)).findAll(Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(3)
	void testFindAllNotificationNotFound() throws IOException {
		
		Page<Notification> p = new PageImpl<>(Arrays.asList());
		Mockito.when(this.notificationRepository.findAll(Mockito.any(Pageable.class))).thenReturn(p);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findAllNotification(PageRequest.of(0, 10));
		});
		
		verify(this.notificationRepository, times(1)).findAll(Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(4)
	void testFindNotReadNotification() throws IOException {
		
		List<Notification> list = Arrays.asList(Notification.builder()
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.build());
		Mockito.when(this.notificationRepository.findOrphanNotification(Mockito.anyString())).thenReturn(list);
		
		List<NotificationDto> result = this.service.findNotReadNotification("1");
		
		assertThat(result).isNotNull().isNotEmpty().hasSameSizeAs(list);
		
		verify(this.notificationRepository, times(1)).findOrphanNotification(Mockito.anyString());
	}
	
	@Test
	@Order(5)
	void testFindNotReadNotificationEmpty() throws IOException {
		
		List<Notification> list = Arrays.asList();
		Mockito.when(this.notificationRepository.findOrphanNotification(Mockito.anyString())).thenReturn(list);
		
		List<NotificationDto> result = this.service.findNotReadNotification("1");
		
		assertThat(result).isNotNull().isEmpty();
		
		verify(this.notificationRepository, times(1)).findOrphanNotification(Mockito.anyString());
	}
	
	@Test
	@Order(6)
	void testFindNotificationEvents() throws IOException {
		
		Notification n = Notification.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.build();
		it.popso.bicomp.model.User u = it.popso.bicomp.model.User.builder()
				.id(BigDecimal.ONE)
				.registrationNumber("0001")
				.build();
		List<NotificationUser> list = Arrays.asList(NotificationUser.builder()
				.notification(n)
				.user(u)
				.tmsRead(new Date())
				.build());
		Mockito.when(this.notificationUserRepository.findNotificationEvents(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		List<NotificationEventDto> result = this.service.findNotificationEvents(BigDecimal.ONE);
		
		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.get(0).getNotificationId()).isEqualTo(n.getId());
		assertThat(result.get(0).getRegistrationNumber()).isEqualTo(u.getRegistrationNumber());
		
		verify(this.notificationUserRepository, times(1)).findNotificationEvents(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(7)
	void testFindNotificationEventsEmpty() throws IOException {
		
		List<NotificationUser> list = Arrays.asList();
		Mockito.when(this.notificationUserRepository.findNotificationEvents(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findNotificationEvents(BigDecimal.ONE);
		});
		
		verify(this.notificationUserRepository, times(1)).findNotificationEvents(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(8)
	void testReadNotification() throws IOException {
		
		NotificationUser nu = NotificationUser.builder()
				.tmsRead(new Date())
				.build();
		Notification n = Notification.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.notificationUsers(new HashSet<>(Arrays.asList(nu)))
				.build();
		it.popso.bicomp.model.User u = it.popso.bicomp.model.User.builder()
				.id(BigDecimal.ONE)
				.registrationNumber("0001")
				.notificationUsers(new HashSet<>(Arrays.asList(nu)))
				.build();
		nu.setNotification(n);
		nu.setUser(u);
		
		Mockito.when(this.notificationRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(n));
		Mockito.when(this.userRepository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.of(u));
		Mockito.when(this.notificationUserRepository.save(Mockito.any(NotificationUser.class))).thenReturn(nu);
		Mockito.when(this.notificationRepository.save(Mockito.any(Notification.class))).thenReturn(n);
		Mockito.when(this.userRepository.save(Mockito.any(it.popso.bicomp.model.User.class))).thenReturn(u);
		
		this.service.readNotification(new BigDecimal[] { BigDecimal.ONE }, "1");
		
		verify(this.notificationRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.userRepository, times(1)).findByRegistrationNumber(Mockito.anyString());
		verify(this.notificationUserRepository, times(1)).save(Mockito.any(NotificationUser.class));
		verify(this.notificationRepository, times(1)).save(Mockito.any(Notification.class));
		verify(this.userRepository, times(1)).save(Mockito.any(it.popso.bicomp.model.User.class));
	}
	
	@Test
	@Order(9)
	void testReadNotificationNotificationNotFound() throws IOException {
		
		Mockito.when(this.notificationRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.readNotification(new BigDecimal[] { BigDecimal.ONE }, "1");
		});
		
		verify(this.notificationRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.userRepository, times(0)).findByRegistrationNumber(Mockito.anyString());
		verify(this.notificationUserRepository, times(0)).save(Mockito.any(NotificationUser.class));
		verify(this.notificationRepository, times(0)).save(Mockito.any(Notification.class));
		verify(this.userRepository, times(0)).save(Mockito.any(it.popso.bicomp.model.User.class));
	}
	
	@Test
	@Order(10)
	void testReadNotificationUserNotFound() throws IOException {
		
		Notification n = Notification.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.build();
		
		Mockito.when(this.notificationRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(n));
		Mockito.when(this.userRepository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.readNotification(new BigDecimal[] { BigDecimal.ONE }, "1");
		});
		
		verify(this.notificationRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.userRepository, times(1)).findByRegistrationNumber(Mockito.anyString());
		verify(this.notificationUserRepository, times(0)).save(Mockito.any(NotificationUser.class));
		verify(this.notificationRepository, times(0)).save(Mockito.any(Notification.class));
		verify(this.userRepository, times(0)).save(Mockito.any(it.popso.bicomp.model.User.class));
	}
	
	@Test
	@Order(11)
	void testReadMultipleNotifications() throws IOException {
		
		NotificationUser nu = NotificationUser.builder()
				.tmsRead(new Date())
				.build();
		Notification n = Notification.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.notificationUsers(new HashSet<>(Arrays.asList(nu)))
				.build();
		it.popso.bicomp.model.User u = it.popso.bicomp.model.User.builder()
				.id(BigDecimal.ONE)
				.registrationNumber("0001")
				.notificationUsers(new HashSet<>(Arrays.asList(nu)))
				.build();
		nu.setNotification(n);
		nu.setUser(u);
		BigDecimal[] ids = new BigDecimal[] { new BigDecimal(1), new BigDecimal(2) };
		
		Mockito.when(this.notificationRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.of(n));
		Mockito.when(this.userRepository.findByRegistrationNumber(Mockito.anyString())).thenReturn(Optional.of(u));
		Mockito.when(this.notificationUserRepository.save(Mockito.any(NotificationUser.class))).thenReturn(nu);
		Mockito.when(this.notificationRepository.save(Mockito.any(Notification.class))).thenReturn(n);
		Mockito.when(this.userRepository.save(Mockito.any(it.popso.bicomp.model.User.class))).thenReturn(u);
		
		this.service.readNotification(ids, "1");
		
		verify(this.notificationRepository, times(ids.length)).findById(Mockito.any(BigDecimal.class));
		verify(this.userRepository, times(ids.length)).findByRegistrationNumber(Mockito.anyString());
		verify(this.notificationUserRepository, times(ids.length)).save(Mockito.any(NotificationUser.class));
		verify(this.notificationRepository, times(ids.length)).save(Mockito.any(Notification.class));
		verify(this.userRepository, times(ids.length)).save(Mockito.any(it.popso.bicomp.model.User.class));
	}
	
	@Test
	@Order(12)
	void testReadMultipleNotificationsEmpty() throws IOException {
		
		BigDecimal[] ids = new BigDecimal[] {};
		
		this.service.readNotification(ids, "1");
		
		verify(this.notificationRepository, times(0)).findById(Mockito.any(BigDecimal.class));
		verify(this.userRepository, times(0)).findByRegistrationNumber(Mockito.anyString());
		verify(this.notificationUserRepository, times(0)).save(Mockito.any(NotificationUser.class));
		verify(this.notificationRepository, times(0)).save(Mockito.any(Notification.class));
		verify(this.userRepository, times(0)).save(Mockito.any(it.popso.bicomp.model.User.class));
	}
	
	@Test
	@Order(13)
	void testReadMultipleNotificationsNotFound() throws IOException {
		
		BigDecimal[] ids = new BigDecimal[] { new BigDecimal(1) };
		
		Mockito.when(this.notificationRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.readNotification(ids, "1");
		});
		
		verify(this.notificationRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.userRepository, times(0)).findByRegistrationNumber(Mockito.anyString());
		verify(this.notificationUserRepository, times(0)).save(Mockito.any(NotificationUser.class));
		verify(this.notificationRepository, times(0)).save(Mockito.any(Notification.class));
		verify(this.userRepository, times(0)).save(Mockito.any(it.popso.bicomp.model.User.class));
	}
	
	@Test
	@Order(14)
	void testDeleteNotifications() throws IOException {
		
		Mockito.doNothing().when(this.notificationRepository).deleteOldNotifications(Mockito.anyInt());
		
		this.service.deleteNotifications();
		
		verify(this.notificationRepository, times(1)).deleteOldNotifications(Mockito.anyInt());
	}

}