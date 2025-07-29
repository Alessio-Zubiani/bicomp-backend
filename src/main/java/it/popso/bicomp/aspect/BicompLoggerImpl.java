package it.popso.bicomp.aspect;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.popso.bicomp.utils.BicompConstants;

@Aspect
@Component
public class BicompLoggerImpl {	
	
	@Around("execution(* *(..)) && @annotation(bicompLogger)")
	public Object bicompLogger(ProceedingJoinPoint proceedingJoinPoint, BicompLogger bicompLogger) throws Throwable {
		
	    Class<? extends Object> targetClass = proceedingJoinPoint.getTarget().getClass();
	    Logger log = LoggerFactory.getLogger(targetClass);
	    String descrizione = StringUtils.defaultIfBlank(bicompLogger.value(), targetClass.getSimpleName());
		
		if(bicompLogger.enabled()) {
			log.info(BicompConstants.BEFORE_METHOD_LOG_FORMAT, descrizione.replaceAll(BicompConstants.LOG_CRLF, ""));
		    
			//Extracting method signature
			CodeSignature codeSignature = (CodeSignature) proceedingJoinPoint.getSignature();
			
			//Extracting method arguments name
			String[] parameterNames = codeSignature.getParameterNames();
			
			//Extracting method arguments value
			Object[] args = proceedingJoinPoint.getArgs();
			
			
			Map<String, Object> fieldToValue = new HashMap<>();
			for(int i = 0; i < parameterNames.length; ++i) {
			    fieldToValue.put(parameterNames[i], args[i]);
			}
			fieldToValue.forEach((key, value) -> log.info("Arg: [{} = {}]", key, value));
			
			Object object = proceedingJoinPoint.proceed();
			log.info(BicompConstants.AFTER_METHOD_LOG_FORMAT, descrizione.replaceAll(BicompConstants.LOG_CRLF, ""));
			
		    return object;
		}
		else {
			log.warn(BicompConstants.BEFORE_METHOD_NO_LOG_FORMAT, descrizione.replaceAll(BicompConstants.LOG_CRLF, ""), "but logging is disabled".replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return proceedingJoinPoint.proceed();
		}
	}
	
	@AfterThrowing(pointcut = ("execution(* *(..)) && @annotation(bicompLogger)"), throwing = "exception")
	public void bicompLoggerError(JoinPoint joinPoint, BicompLogger bicompLogger, Throwable exception) {
		
		Class<? extends Object> targetClass = joinPoint.getTarget().getClass();
	    Logger log = LoggerFactory.getLogger(targetClass);
	    String descrizione = StringUtils.defaultIfBlank(bicompLogger.value(), targetClass.getSimpleName());
		
		log.error(BicompConstants.ERROR_LOG_FORMAT, descrizione.replaceAll(BicompConstants.LOG_CRLF, ""), exception.getMessage() != null ? exception.getMessage().replaceAll(BicompConstants.LOG_CRLF, "") : exception);
	    log.error(BicompConstants.AFTER_METHOD_LOG_FORMAT, descrizione.replaceAll(BicompConstants.LOG_CRLF, ""));
	}

}
