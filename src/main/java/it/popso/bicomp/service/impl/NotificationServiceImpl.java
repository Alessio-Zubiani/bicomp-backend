package it.popso.bicomp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.popso.bicomp.dto.NotificationDto;
import it.popso.bicomp.dto.NotificationEventDto;
import it.popso.bicomp.dto.PageableNotificationDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Notification;
import it.popso.bicomp.model.NotificationUser;
import it.popso.bicomp.model.NotificationUserKey;
import it.popso.bicomp.repository.NotificationRepository;
import it.popso.bicomp.repository.NotificationUserRepository;
import it.popso.bicomp.repository.UserRepository;
import it.popso.bicomp.service.EmitterService;
import it.popso.bicomp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final NotificationUserRepository notificationUserRepository;
	private final EmitterService emitterService;
	
	
	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { DataIntegrityViolationException.class }
	)
	public void createNotification(String memberId, String title, String message, String livello) {
		
		Notification n = this.notificationRepository.save(Notification.builder()
				.tmsInsert(new Date())
				.livello(livello)
				.message(message)
				.title(title)
				.build());
		
		this.emitterService.sendEvent(n);
	}

	@Override
	public PageableNotificationDto findAllNotification(Pageable paging) throws ResourceNotFoundException {
		
		Page<Notification> page = this.notificationRepository.findAll(paging);
		if(page.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Notification found").toString());
		}
		
		PageableNotificationDto p = PageableNotificationDto.builder().build();
		List<NotificationDto> notifications = new ArrayList<>();
		page.getContent().forEach(n -> notifications.add(this.notificationToNotificationDto(n)));
		p.setTotalElements(new BigDecimal(page.getTotalElements()));
		p.setEntries(notifications);
		
		return p;
	}
	
	@Override
	public List<NotificationDto> findNotReadNotification(String principal) throws ResourceNotFoundException {
		
		List<Notification> list = this.notificationRepository.findOrphanNotification(principal);
		
		List<NotificationDto> notifications = new ArrayList<>();
		list.forEach(n -> notifications.add(this.notificationToNotificationDto(n)));
		
		return notifications;
	}

	@Override
	public List<NotificationEventDto> findNotificationEvents(BigDecimal id) throws ResourceNotFoundException {
		
		List<NotificationUser> list = this.notificationUserRepository.findNotificationEvents(id);
		if(list.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No events found for notification id: [").append(id).append("]").toString());
		}
		
		List<NotificationEventDto> result = new ArrayList<>();
		list.forEach(nu -> {
			log.info("Notification: {}", nu.getNotification());
			log.info("User: {}", nu.getUser());
			result.add(this.notificationUserToNotificationEventDto(nu));
		});
		
		return result;
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { DataIntegrityViolationException.class, ResourceNotFoundException.class }
	)
	public void readNotification(BigDecimal[] ids, String principal) throws ResourceNotFoundException {
		
		for(BigDecimal id : ids) {
			Optional<Notification> n = this.notificationRepository.findById(id);
			if(n.isEmpty()) {
				throw new ResourceNotFoundException(new StringBuilder("No Notification found for ID: [")
						.append(id).append("]").toString());
			}
			
			Optional<it.popso.bicomp.model.User> u = this.userRepository.findByRegistrationNumber(principal);
			if(u.isEmpty()) {
				throw new ResourceNotFoundException(new StringBuilder("No User found for Username: [")
						.append(principal).append("]").toString());
			}
			
			Notification notification = n.get();
			it.popso.bicomp.model.User user = u.get();
			
			NotificationUser nu = NotificationUser.builder()
					.id(NotificationUserKey.builder()
							.notificationId(notification.getId())
							.userId(user.getId())
							.build())
					.notification(notification)
					.user(user)
					.tmsRead(new Date())
					.build();
			
			notification.getNotificationUsers().add(nu);
			user.getNotificationUsers().add(nu);
			
			this.notificationUserRepository.save(nu);
			this.notificationRepository.save(notification);
			this.userRepository.save(user);
		}
	}
	
	@Override
	@Transactional
	public void deleteNotifications() {
		
		this.notificationRepository.deleteOldNotifications(10);
	}
	
	private NotificationDto notificationToNotificationDto(Notification notification) {
		
		return NotificationDto.builder()
				.id(notification.getId())
				.tmsInsert(notification.getTmsInsert())
				.livello(notification.getLivello())
				.message(notification.getMessage())
				.title(notification.getTitle())
				.build();
	}
	
	private NotificationEventDto notificationUserToNotificationEventDto(NotificationUser notificationUser) {
		
		return NotificationEventDto.builder()
				.notificationId(notificationUser.getNotification().getId())
				.tmsRead(notificationUser.getTmsRead())
				.registrationNumber(notificationUser.getUser().getRegistrationNumber())
				.build();
	}

}
