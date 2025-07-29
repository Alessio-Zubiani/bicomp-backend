package it.popso.bicomp.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the NOTIFICATION_USER database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "NOTIFICATION_USER")
public class NotificationUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private NotificationUserKey id;
	
	@ManyToOne
    @MapsId("notificationId")
    @JoinColumn(name = "NOTIFICATION_ID")
    private Notification notification;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_READ")
	private Date tmsRead;

}