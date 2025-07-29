package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.NotificationUser;
import it.popso.bicomp.model.NotificationUserKey;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, NotificationUserKey> {
	
	@Query("SELECT nu FROM NotificationUser nu JOIN Notification n ON nu.id.notificationId = n.id JOIN User u ON nu.id.userId = u.id WHERE nu.id.notificationId = ?1 ORDER BY nu.tmsRead DESC")
	List<NotificationUser> findNotificationEvents(BigDecimal notificationId);
	
}
