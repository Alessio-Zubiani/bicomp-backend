package it.popso.bicomp.dto;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class UserDto {
	
	private BigDecimal id;
	
	@NotNull
	@NotBlank
	private String registrationNumber;
	
	@NotNull
	@NotBlank
	private String name;
	
	@NotNull
	@NotBlank
	private String surname;
	private Date creationDateTime;
	private Date lastLoginDateTime;
	private String codiceAreaDipendenza;
	private String codiceDipendenza;
	
	@NotNull
	@NotBlank
	private String email;
	private String phone;
	private String mobilePhone;
	private String role;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
