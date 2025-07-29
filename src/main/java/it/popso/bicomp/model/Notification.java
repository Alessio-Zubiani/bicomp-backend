package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the NOTIFICATION database table.
 * 
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "NOTIFICATION")
public class Notification implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "NOTIFICATION_ID_GENERATOR", sequenceName = "NOTIFICATION_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTIFICATION_ID_GENERATOR")
	private BigDecimal id;
	
	@Column(name = "LIVELLO")
	private String livello;

	@Lob
    @Column(name = "MESSAGE", columnDefinition = "CLOB")
	private String message;
	
	@Column(name = "TITLE")
	private String title;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TMS_INSERT", updatable = false)
	private Date tmsInsert;

	@OneToMany(mappedBy = "notification")
	private Set<NotificationUser> notificationUsers;

}