package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * The NOTIFICATION_USER composite key.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class NotificationUserKey implements Serializable {
	private static final long serialVersionUID = 1L;
		
	@Column(name = "NOTIFICATION_ID")
	private BigDecimal notificationId;
	
	@Column(name = "USER_ID")
	private BigDecimal userId;
	

	@Override
	public int hashCode() {
		return Objects.hash(notificationId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificationUserKey other = (NotificationUserKey) obj;
		
		return Objects.equals(notificationId, other.notificationId) && Objects.equals(userId, other.userId);
	}

}