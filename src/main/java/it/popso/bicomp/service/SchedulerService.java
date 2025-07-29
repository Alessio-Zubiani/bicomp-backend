package it.popso.bicomp.service;

import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.model.Timer;

public interface SchedulerService {
	
	void enable(Timer t) throws BicompException;
	
	void disable(Timer t) throws BicompException;
	
	void updateScheduling(Timer t) throws BicompException;
	
	void runNow(Timer t) throws BicompException;
	
	void unscheduleAndDelete(Timer t) throws BicompException;

}
