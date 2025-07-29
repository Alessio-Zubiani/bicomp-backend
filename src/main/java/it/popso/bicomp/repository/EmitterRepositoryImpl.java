package it.popso.bicomp.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Repository
@RequiredArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {
	
	private Map<String, SseEmitter> userEmitterMap = new ConcurrentHashMap<>();
	

	@Override
	public void addOrReplaceEmitter(String memberId, SseEmitter emitter) {
		
		this.userEmitterMap.put(memberId, emitter);
	}

	@Override
	public void remove(String memberId) {
		
		if(this.userEmitterMap != null && this.userEmitterMap.containsKey(memberId)) {
            log.info("Removing emitter for member: [{}]", memberId);
            this.userEmitterMap.remove(memberId);
        } 
		else {
            log.info("No emitter to remove for member: [{}]", memberId);
        }
	}

	@Override
	public Optional<SseEmitter> get(String memberId) {
		
		return Optional.ofNullable(this.userEmitterMap.get(memberId));
	}

}
