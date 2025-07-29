package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, BigDecimal>, PagingAndSortingRepository<Notification, BigDecimal> {
	
	Optional<Notification> findById(BigDecimal id);
	
	//Page<Notification> findAllByOrderByTmsInsertDesc(Pageable paging);
	Page<Notification> findAll(Pageable paging);
	
	@Query("SELECT n FROM Notification n WHERE n.id NOT IN (SELECT nu.id.notificationId FROM NotificationUser nu JOIN User u ON nu.id.userId = u.id WHERE u.registrationNumber = ?1) ORDER BY n.tmsInsert DESC")
	List<Notification> findOrphanNotification(String registrationNumber);
	
	@Procedure(procedureName = "delete_old_notifications")
    void deleteOldNotifications(int days);
	
}
