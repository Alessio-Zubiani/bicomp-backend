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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.popso.bicomp.dto.NotificationDto;
import it.popso.bicomp.dto.NotificationEventDto;
import it.popso.bicomp.dto.PageableNotificationDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.EmitterService;
import it.popso.bicomp.service.NotificationService;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class NotificationControllerTest {
	
	@Mock
	private NotificationService notificationService;
	
	@Mock
	private EmitterService emitterService;
	
	@Mock
	private Authentication authentication;
	
	private NotificationController controller;
	

	@BeforeEach
	public void setup() {
		this.controller = new NotificationController(this.notificationService, this.emitterService);
	}
	
	@Test
	@Order(1)
	void testSubscribeNotification() {
		
		Mockito.when(this.emitterService.createEmitter()).thenReturn(new SseEmitter());
		
		SseEmitter result = this.controller.subscribeNotifications();
		
		assertThat(result).isNotNull();
		
		verify(this.emitterService, times(1)).createEmitter();
	}
	
	@Test
	@Order(2)
	void testPublishEvent() {
		
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.controller.publishEvent();
		
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(3)
	void testGetAllNotifications() {
		
		PageableNotificationDto p = PageableNotificationDto.builder()
				.totalElements(BigDecimal.ONE)
				.entries(Arrays.asList(NotificationDto.builder()
							.tmsInsert(new Date())
							.livello("INFO")
							.message("Test Notification")
							.title("Test")
							.build()))
				.build();
		Mockito.when(this.notificationService.findAllNotification(Mockito.any(PageRequest.class))).thenReturn(p);
		
		PageableNotificationDto result = (PageableNotificationDto) this.controller.getAllNotifications(0, 10).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).hasSize(1);
		
		verify(this.notificationService, times(1)).findAllNotification(Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(4)
	void testGetAllNotificationsNotFound() {
		
		Mockito.when(this.notificationService.findAllNotification(Mockito.any(PageRequest.class))).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getAllNotifications(0, 10);
		});
		
		verify(this.notificationService, times(1)).findAllNotification(Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(5)
	void testGetNotReadNotifications() {
		
		List<NotificationDto> list = Arrays.asList(NotificationDto.builder()
				.tmsInsert(new Date())
				.livello("INFO")
				.message("Test Notification")
				.title("Test")
				.build());
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.when(this.notificationService.findNotReadNotification(Mockito.anyString())).thenReturn(list);
		
		List<NotificationDto> result = (List<NotificationDto>) this.controller.getNotReadNotifications(this.authentication).getBody().getResponse();
		
		assertThat(result).isNotNull().hasSameSizeAs(list);
		
		verify(this.notificationService, times(1)).findNotReadNotification(Mockito.anyString());
	}
	
	@Test
	@Order(6)
	void testGetNotReadNotificationsNotFound() {
		
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.when(this.notificationService.findNotReadNotification(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getNotReadNotifications(this.authentication);
		});
		
		verify(this.notificationService, times(1)).findNotReadNotification(Mockito.anyString());
	}
	
	@Test
	@Order(7)
	void testGetNotificationEvents() {
		
		List<NotificationEventDto> list = Arrays.asList(NotificationEventDto.builder()
				.notificationId(BigDecimal.ONE)
				.registrationNumber("0001")
				.tmsRead(new Date())
				.build(), NotificationEventDto.builder()
				.notificationId(BigDecimal.ONE)
				.registrationNumber("0001")
				.tmsRead(new Date())
				.build());
		Mockito.when(this.notificationService.findNotificationEvents(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		List<NotificationEventDto> result = (List<NotificationEventDto>) this.controller.getNotificationEvents(BigDecimal.ONE).getBody().getResponse();
		
		assertThat(result).isNotNull().hasSameSizeAs(list);
		
		verify(this.notificationService, times(1)).findNotificationEvents(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(8)
	void testGetNotificationEventsNotFound() {
		
		Mockito.when(this.notificationService.findNotificationEvents(Mockito.any(BigDecimal.class))).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getNotificationEvents(BigDecimal.ONE);
		});
		
		verify(this.notificationService, times(1)).findNotificationEvents(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(9)
	void testReadNotification() {
		
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.doNothing().when(this.notificationService).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
		
		BigDecimal result = (BigDecimal) this.controller.readNotification(BigDecimal.ONE, this.authentication).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(BigDecimal.ONE);
		
		verify(this.notificationService, times(1)).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
	}
	
	@Test
	@Order(10)
	void testReadNotificationNotFound() {
		
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.doThrow(new ResourceNotFoundException("ResourceNotFoundException")).when(this.notificationService).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.readNotification(BigDecimal.ONE, this.authentication);
		});
		
		verify(this.notificationService, times(1)).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
	}
	
	@Test
	@Order(11)
	void testReadMultipleNotifications() {
		
		BigDecimal[] ids = new BigDecimal[] { new BigDecimal(1), new BigDecimal(2) };
		
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.doNothing().when(this.notificationService).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
		
		BigDecimal result = (BigDecimal) this.controller.readMultipleNotifications(ids, this.authentication).getBody().getResponse();
		
		assertThat(result).isNotNull().isEqualTo(new BigDecimal(ids.length));
		
		verify(this.notificationService, times(1)).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
	}
	
	@Test
	@Order(12)
	void testReadMultipleNotificationsNotFound() {
		
		BigDecimal[] ids = new BigDecimal[] { new BigDecimal(1), new BigDecimal(2) };
		
		Mockito.when(this.authentication.getPrincipal()).thenReturn(1);
		Mockito.doThrow(new ResourceNotFoundException("ResourceNotFoundException")).when(this.notificationService).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.readMultipleNotifications(ids, this.authentication);
		});
		
		verify(this.notificationService, times(1)).readNotification(Mockito.any(BigDecimal[].class), Mockito.anyString());
	}
	
}
