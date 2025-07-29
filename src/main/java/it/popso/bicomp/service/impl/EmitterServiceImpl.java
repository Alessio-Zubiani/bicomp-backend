package it.popso.bicomp.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.popso.bicomp.dto.NotificationDto;
import it.popso.bicomp.model.Notification;
import it.popso.bicomp.service.EmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@RequiredArgsConstructor
public class EmitterServiceImpl implements EmitterService {
	
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
	

	@Override
	public SseEmitter createEmitter() {
		
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		emitters.add(emitter);
		
        emitter.onCompletion(() -> {
        	log.info("OnCompletion");
        	emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
        	log.warn("OnTimeout");
        	emitters.remove(emitter);
        });
        emitter.onError(e -> {
            log.error("Create SseEmitter exception", e);
            emitters.remove(emitter);
        });
        
        return emitter;
	}


	@Override
	public void sendEvent(Notification n) {
		
		List<SseEmitter> deadEmitters = new ArrayList<>();
		for(SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name("notification").data(this.notificationToNotificationDto(n)));
			}
			catch(IOException e) {
				deadEmitters.add(emitter);
			}
		}
		
		emitters.removeAll(deadEmitters);
	}
	
	private NotificationDto notificationToNotificationDto(Notification notification) {
		
		return NotificationDto.builder()
				.id(notification.getId())
				.tmsInsert(notification.getTmsInsert())
				.livello(notification.getLivello())
				.message(notification.getMessage())
				.title(notification.getTitle())
				.build();
	}

}
