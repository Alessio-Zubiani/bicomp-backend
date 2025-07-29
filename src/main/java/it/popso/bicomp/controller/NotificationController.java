package it.popso.bicomp.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.NotificationDto;
import it.popso.bicomp.dto.NotificationEventDto;
import it.popso.bicomp.dto.PageableNotificationDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.EmitterService;
import it.popso.bicomp.service.NotificationService;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
	
	private final NotificationService notificationService;
	private final EmitterService emitterService;
	
	
	@GetMapping("/subscription")
	@BicompLogger
	public SseEmitter subscribeNotifications() {
		
		log.info("Notification subscription");
		return this.emitterService.createEmitter();
	}
	
	@PostMapping("/send")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@BicompLogger
    public void publishEvent() {
        log.info("Publishing event");
        this.notificationService.createNotification("BICOMP", "title", "message", "level");
	}
	
	@GetMapping("/all")
	@BicompLogger
	public ResponseEntity<Response<PageableNotificationDto>> getAllNotifications(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size) throws ResourceNotFoundException {
		
		PageableNotificationDto notifications = this.notificationService.findAllNotification(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "tmsInsert")));
		return ResponseEntity.ok()
				.body(Response.<PageableNotificationDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(notifications)
					.message("Notifications retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/not-read")
	@BicompLogger
	public ResponseEntity<Response<List<NotificationDto>>> getNotReadNotifications(Authentication authentication) throws ResourceNotFoundException {
		
		Integer principal = (Integer) authentication.getPrincipal();
		
		List<NotificationDto> notifications = this.notificationService.findNotReadNotification(String.valueOf(principal));
		return ResponseEntity.ok()
				.body(Response.<List<NotificationDto>>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(notifications)
					.message("Not read notifications retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/{id}/events")
	@BicompLogger
	public ResponseEntity<Response<List<NotificationEventDto>>> getNotificationEvents(@PathVariable("id") BigDecimal id) throws ResourceNotFoundException {
		
		List<NotificationEventDto> notifications = this.notificationService.findNotificationEvents(id);
		return ResponseEntity.ok()
				.body(Response.<List<NotificationEventDto>>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(notifications)
					.message("Notification events retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping("/read/{id}")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> readNotification(@PathVariable("id") BigDecimal id, Authentication authentication) throws ResourceNotFoundException {
		
		Integer principal = (Integer) authentication.getPrincipal();
		
		this.notificationService.readNotification(new BigDecimal[] { id }, String.valueOf(principal));
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(id)
					.message("Notification updated successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@PostMapping("/read/multiple")
	@BicompLogger
	public ResponseEntity<Response<BigDecimal>> readMultipleNotifications(@RequestBody BigDecimal[] ids, Authentication authentication) throws ResourceNotFoundException {
		
		Integer principal = (Integer) authentication.getPrincipal();
		
		this.notificationService.readNotification(ids, String.valueOf(principal));
		return ResponseEntity.ok()
				.body(Response.<BigDecimal>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(new BigDecimal(ids.length))
					.message("Succesfully updated [".concat(new BigDecimal(ids.length).toString()).concat("] notifications"))
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}

}
