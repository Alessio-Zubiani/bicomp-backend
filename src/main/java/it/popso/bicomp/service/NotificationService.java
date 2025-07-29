package it.popso.bicomp.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;

import it.popso.bicomp.dto.NotificationDto;
import it.popso.bicomp.dto.NotificationEventDto;
import it.popso.bicomp.dto.PageableNotificationDto;
import it.popso.bicomp.exception.ResourceNotFoundException;

public interface NotificationService {
	
	void createNotification(String memberId, String title, String message, String livello);
	
	PageableNotificationDto findAllNotification(Pageable paging) throws ResourceNotFoundException;
	
	List<NotificationDto> findNotReadNotification(String principal) throws ResourceNotFoundException;
	
	List<NotificationEventDto> findNotificationEvents(BigDecimal id) throws ResourceNotFoundException;
	
	void readNotification(BigDecimal[] ids, String principal) throws ResourceNotFoundException;
	
	void deleteNotifications();

}
