package it.popso.bicomp.service;

import java.math.BigDecimal;
import java.util.List;

import it.popso.bicomp.dto.FeedbackDto;
import it.popso.bicomp.dto.FeedbackEventiDto;
import it.popso.bicomp.exception.ResourceNotFoundException;


public interface FeedbackService {
	
	List<FeedbackDto> findAllFeedback() throws ResourceNotFoundException;
	
	FeedbackDto findById(BigDecimal id) throws ResourceNotFoundException;
	
	FeedbackDto createFeedback(FeedbackDto f);
	
	List<FeedbackEventiDto> findEventsByFeedbackId(BigDecimal id);
	
	void updateFeedback(BigDecimal id, FeedbackDto f) throws ResourceNotFoundException;

}
