package it.popso.bicomp.model;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Set;


/**
 * The persistent class for the USERS database table.
 * 
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name = "USERS", uniqueConstraints = {
	@UniqueConstraint(name = "USERS_REGISTRATION_NUMBER_IDX", columnNames = {"REGISTRATION_NUMBER"})
})
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "USERS_ID_GENERATOR", sequenceName = "USERS_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_ID_GENERATOR")
	private BigDecimal id;

	@Column(name = "CODICE_AREA_DIPENDENZA")
	private String codiceAreaDipendenza;

	@Column(name = "CODICE_DIPENDENZA")
	private String codiceDipendenza;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE_TIME")
	private Date creationDateTime;
	
	@Column(name = "EMAIL")
	private String email;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_LOGIN_DATE_TIME")
	private Date lastLoginDateTime;

	@Column(name = "MOBILE_PHONE")
	private String mobilePhone;

	@Column(name = "NAME")
	private String name;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "REGISTRATION_NUMBER")
	private String registrationNumber;

	@Column(name = "SURNAME")
	private String surname;
	
	@ToString.Exclude
	@OneToMany(mappedBy = "user")
	private Set<NotificationUser> notificationUsers;
	
}