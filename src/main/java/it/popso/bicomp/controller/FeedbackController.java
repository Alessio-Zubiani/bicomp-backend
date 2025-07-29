package it.popso.bicomp.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.FeedbackDto;
import it.popso.bicomp.dto.FeedbackEventiDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.FeedbackService;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
	
	private final FeedbackService feedbackService;
	
	
	@GetMapping("/{id}")
	@BicompLogger
	public ResponseEntity<Response<FeedbackDto>> getFeedback(@PathVariable("id") BigDecimal id) throws ResourceNotFoundException {
		
		FeedbackDto feedback = this.feedbackService.findById(id);
		return ResponseEntity.ok()
				.body(Response.<FeedbackDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(feedback)
					.message("Feedback retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}

	@GetMapping
	@BicompLogger
	public ResponseEntity<Response<List<FeedbackDto>>> getAllFeedbacks() throws ResourceNotFoundException {
		
		List<FeedbackDto> feedbacks = this.feedbackService.findAllFeedback();
		return ResponseEntity.ok()
				.body(Response.<List<FeedbackDto>>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(feedbacks)
					.message("Feedbacks retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> createFeedback(@RequestBody FeedbackDto f) throws BicompException {
		
		try {
			FeedbackDto feedback = this.feedbackService.createFeedback(f);
			
			return ResponseEntity.ok()
					.body(Response.<BigDecimal>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(feedback.getId())
						.message(new StringBuilder("Successfully created feedback from [").append(f.getInsertUser()).append("]").toString())
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(DataIntegrityViolationException e) {
			throw new BicompException(e);
		}
	}
	
	@GetMapping("/{id}/events")
	@BicompLogger
	public ResponseEntity<Response<List<FeedbackEventiDto>>> getFeedbackEvents(@PathVariable("id") BigDecimal id) {
		
		List<FeedbackEventiDto> events = this.feedbackService.findEventsByFeedbackId(id);
		return ResponseEntity.ok()
				.body(Response.<List<FeedbackEventiDto>>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(events)
					.message("Feedback events retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping("/{id}")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> updateFeedback(@PathVariable("id") BigDecimal id, @RequestBody FeedbackDto f) throws ResourceNotFoundException {
		
		this.feedbackService.updateFeedback(id, f);
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(f.getId())
					.message(new StringBuilder("Successfully updated feedback with ID [").append(f.getId()).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
}
