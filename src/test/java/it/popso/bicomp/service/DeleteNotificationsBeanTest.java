package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;



@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class DeleteNotificationsBeanTest {
	
	@Mock
	private NotificationService notificationService;
	
	@Mock
	private TimerService timerService;
	
	private DeleteNotificationsBean bean;
	
	
	@BeforeEach
    public void setup() {
		this.bean = new DeleteNotificationsBean(this.timerService, this.notificationService);
	}
	
	@Test
	@Order(1)
	void testExecute() {
		
		Mockito.doNothing().when(this.notificationService).deleteNotifications();
		
		this.bean.execute();
		
		verify(this.notificationService, times(1)).deleteNotifications();
	}

}
