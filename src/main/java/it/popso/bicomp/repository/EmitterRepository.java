package it.popso.bicomp.repository;

import java.util.Optional;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {
	
	void addOrReplaceEmitter(String memberId, SseEmitter emitter);

    void remove(String memberId);

    Optional<SseEmitter> get(String memberId);

}
