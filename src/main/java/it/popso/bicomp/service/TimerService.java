package it.popso.bicomp.service;

import java.math.BigDecimal;
import java.util.List;

import it.popso.bicomp.dto.TimerDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;


public interface TimerService {
	
	List<TimerDto> findAllTimer() throws ResourceNotFoundException;
	
	Timer findById(BigDecimal id) throws ResourceNotFoundException;
	
	void createTimer(TimerDto t);
	
	void scheduleTimer() throws BicompException;
	
	void enableTimer(BigDecimal id) throws BicompException, ResourceNotFoundException;
	
	void disableTimer(BigDecimal id) throws BicompException, ResourceNotFoundException;
	
	void updateTimerScheduling(TimerDto t) throws BicompException, ResourceNotFoundException;
	
	void executeTimer(BigDecimal id) throws BicompException, ResourceNotFoundException;
	
	void unscheduleAndDeleteTimer(BigDecimal id) throws BicompException, ResourceNotFoundException;
	
	Timer findByJobName(String jobName) throws ResourceNotFoundException;
	
	void updateTimer(Timer t) throws ResourceNotFoundException;

}
