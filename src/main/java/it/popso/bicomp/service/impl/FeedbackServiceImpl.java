package it.popso.bicomp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.popso.bicomp.dto.FeedbackDto;
import it.popso.bicomp.dto.FeedbackEventiDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Feedback;
import it.popso.bicomp.model.FeedbackEventi;
import it.popso.bicomp.repository.FeedbackEventiRepository;
import it.popso.bicomp.repository.FeedbackRepository;
import it.popso.bicomp.service.FeedbackService;
import it.popso.bicomp.utils.FeedbackStatus;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

	private final FeedbackRepository feedbackRepository;
	private final FeedbackEventiRepository feedbackEventiRepository;
	private final ModelMapper modelMapper;
	
	
	@Override
	public List<FeedbackDto> findAllFeedback() throws ResourceNotFoundException {
		
		List<Feedback> list = this.feedbackRepository.findAllByOrderByTmsInsertDesc();
		if(list.isEmpty()) {
			throw new ResourceNotFoundException("No feedback found");
		}
		
		List<FeedbackDto> feedbacks = new ArrayList<>();
		list.forEach(f -> feedbacks.add(this.modelMapper.map(f, FeedbackDto.class)));
		
		return feedbacks;
	}

	@Override
	public FeedbackDto findById(BigDecimal id) throws ResourceNotFoundException {
		
		Optional<Feedback> o = this.feedbackRepository.findById(id);
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("Feedback with ID [").append(id).append("] not found").toString());
		}
		
		return this.modelMapper.map(o.get(), FeedbackDto.class);
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { DataIntegrityViolationException.class }
	)
	@Override
	public FeedbackDto createFeedback(FeedbackDto f) {
		
		Feedback feedback = this.modelMapper.map(f, Feedback.class);
		feedback.setStato(FeedbackStatus.INSERITO.name());
		this.feedbackRepository.save(feedback);
		
		FeedbackEventi feedbackEventi = FeedbackEventi.builder()
				.descrizione(f.getDescrizione())
				.insertUser(f.getInsertUser())
				.stato(FeedbackStatus.INSERITO.name())
				.feedback(feedback)
				.build();
		this.feedbackEventiRepository.save(feedbackEventi);
		
		return this.modelMapper.map(feedback, FeedbackDto.class);
	}

	@Override
	public List<FeedbackEventiDto> findEventsByFeedbackId(BigDecimal id) {
		
		List<FeedbackEventi> list = this.feedbackEventiRepository.findByFeedbackIdOrderByTmsInsertDesc(id);
		
		List<FeedbackEventiDto> events = new ArrayList<>();
		list.forEach(e -> events.add(this.modelMapper.map(e, FeedbackEventiDto.class)));
		
		return events;
	}

	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { DataIntegrityViolationException.class, ResourceNotFoundException.class }
	)
	@Override
	public void updateFeedback(BigDecimal id, FeedbackDto f) throws ResourceNotFoundException {
		
		Optional<Feedback> o = this.feedbackRepository.findById(id);
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("Feedback with ID [").append(id).append("] not found").toString());
		}
		
		Feedback feedback = o.get();
		feedback.setUpdateUser(f.getUpdateUser());
		feedback.setStato(f.getStato());
		this.feedbackRepository.save(feedback);
		
		FeedbackEventi feedbackEventi = FeedbackEventi.builder()
				.descrizione(f.getDescrizione())
				.insertUser(f.getUpdateUser())
				.stato(f.getStato())
				.feedback(feedback)
				.build();
		this.feedbackEventiRepository.save(feedbackEventi);
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.feedbackRepository == null) ? 0 : this.feedbackRepository.hashCode());
	    result = prime * result + ((this.feedbackEventiRepository == null) ? 0 : this.feedbackEventiRepository.hashCode());
	    result = prime * result + ((this.modelMapper == null) ? 0 : this.modelMapper.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof FeedbackServiceImpl))
	        return false;
	    
	    FeedbackServiceImpl other = (FeedbackServiceImpl) obj;
	    boolean f = (this.feedbackRepository == null && other.feedbackRepository == null) || (this.feedbackRepository != null && this.feedbackRepository.equals(other.feedbackRepository));
	    boolean fe = (this.feedbackEventiRepository == null && other.feedbackEventiRepository == null) 
	    		|| (this.feedbackEventiRepository != null && this.feedbackEventiRepository.equals(other.feedbackEventiRepository));
	    boolean m = (this.modelMapper == null && other.modelMapper == null) 
	    		|| (this.modelMapper != null && this.modelMapper.equals(other.modelMapper));
	    return f && fe && m;
	}

}
