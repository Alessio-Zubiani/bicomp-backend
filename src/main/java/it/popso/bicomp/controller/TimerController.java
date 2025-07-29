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
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.dto.TimerDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Timer;
import it.popso.bicomp.service.TimerService;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/timers")
@RequiredArgsConstructor
public class TimerController {
	
	private final TimerService timerService;
	
	
	@GetMapping("/{id}")
	@BicompLogger
	public ResponseEntity<Response<TimerDto>> getTimer(@PathVariable("id") BigDecimal id) throws ResourceNotFoundException {
		
		TimerDto timer = this.createDto(this.timerService.findById(id));
		return ResponseEntity.ok()
				.body(Response.<TimerDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(timer)
					.message("Timer retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}

	@GetMapping
	@BicompLogger
	public ResponseEntity<Response<List<TimerDto>>> getAllTimers() throws ResourceNotFoundException {
		
		List<TimerDto> timers = this.timerService.findAllTimer();
		return ResponseEntity.ok()
				.body(Response.<List<TimerDto>>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(timers)
					.message("Timers retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping
	@BicompLogger
	public ResponseEntity<Response<String>> createTimer(@RequestBody TimerDto t) throws BicompException {
		
		try {
			this.timerService.createTimer(t);
			
			return ResponseEntity.ok()
					.body(Response.<String>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(t.getJobName())
						.message(new StringBuilder("Successfully created timer [").append(t.getJobName()).append("]").toString())
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(DataIntegrityViolationException e) {
			throw new BicompException(e);
		}
	}
	
	@PostMapping(value = "/{id}/enable")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> enableTimer(@PathVariable("id") BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		this.timerService.enableTimer(id);
		
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(id)
					.message(new StringBuilder("Successfully enabled timer with id: [").append(id).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping(value = "/{id}/disable")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> disableTimer(@PathVariable("id") BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		this.timerService.disableTimer(id);
		
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(id)
					.message(new StringBuilder("Successfully disabled timer with ID: [").append(id).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping(value = "/update")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> updateTimer(@RequestBody TimerDto t) throws BicompException, ResourceNotFoundException {
		
		this.timerService.updateTimerScheduling(t);
		
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(t.getJobId())
					.message(new StringBuilder("Successfully updated timer with ID: [").append(t.getJobId()).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping(value = "/{id}/start")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> executeTimer(@PathVariable("id") BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		this.timerService.executeTimer(id);
		
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(id)
					.message(new StringBuilder("Successfully run now timer with ID: [").append(id).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping(value = "/{id}/delete")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> deleteTimer(@PathVariable("id") BigDecimal id) throws BicompException, ResourceNotFoundException {
		
		this.timerService.unscheduleAndDeleteTimer(id);
		
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(id)
					.message(new StringBuilder("Successfully deleted timer with ID: [").append(id).append("]").toString())
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	private TimerDto createDto(Timer t) {
		return TimerDto.builder()
				.jobId(t.getJobId())
				.jobName(t.getJobName())
				.jobGroup(t.getJobGroup())
				.jobStatus(t.getJobStatus())
				.lastExecutionStatus(t.getLastExecutionStatus())
				.jobClass(t.getJobClass())
				.cronExpression(t.getCronExpression())
				.jobDescription(t.getJobDescription())
				.interfaceName(t.getInterfaceName())
				.lastStart(t.getLastStart())
				.lastStop(t.getLastStop())
				.cronJob(t.getCronJob())
				.enabled(t.getEnabled())
				.build();
	}
	
}
