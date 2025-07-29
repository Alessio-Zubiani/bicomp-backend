package it.popso.bicomp.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class Response<T> {
	
	private LocalDateTime timeStamp;
	private int statusCode;
	private String status;
	private String reason;
	private String message;
	private String developerMessage;
	private Boolean isSuccess;
	private T response;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
}
