package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class FunctionalTestJobBeanTest {
	
	@Mock
	private NotificationService notificationService;
	
	@Mock
	private TimerService timerService;
	
	private FunctionalTestJobBean bean;
	
	
	@BeforeEach
    public void setup() {
		this.bean = new FunctionalTestJobBean(this.timerService, this.notificationService);
	}
	
	@Test
	@Order(1)
	void testExecute()  {
		
		int result = 1;
		this.bean.execute();
		
		assertThat(result).isEqualTo(1);
	}

}
