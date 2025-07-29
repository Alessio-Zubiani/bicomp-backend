package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.popso.bicomp.service.impl.EmitterServiceImpl;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class EmitterServiceImplTest {
	
	private EmitterServiceImpl service;
	
	
	@BeforeEach
    public void setup() {
		this.service = new EmitterServiceImpl();
	}
	
	@Test
	@Order(1)
	void testCreateEmitter() throws IOException {
		
		SseEmitter result = this.service.createEmitter();
		assertThat(result).isNotNull();
	}

}
