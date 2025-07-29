package it.popso.bicomp.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.popso.bicomp.model.Notification;

public interface EmitterService {
	
	//SseEmitter createEmitter(String memberId);
	SseEmitter createEmitter();
	
	void sendEvent(Notification n);

}
