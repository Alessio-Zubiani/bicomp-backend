package it.popso.bicomp.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;


public class StringUtils {

	public static StringUtils stringUtils() {
		return new StringUtils();
	}
	
	
	public String customizeExceptionMessage(String className, Exception e) {
		
		String message = e.getMessage();
		String cause = ExceptionUtils.getRootCause(e).getMessage();

		return message.equals(cause) ? className.concat(": ").concat(cause).trim() : className.concat(": ").concat(message).concat(" - ").concat(cause).trim();
	}

}
