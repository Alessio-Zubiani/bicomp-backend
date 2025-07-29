package it.popso.bicomp.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.popso.bicomp.model.TimerStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class TimerDto {
	
	private BigDecimal jobId;
	private String jobName;
	private String jobGroup;
	private TimerStatusEnum jobStatus;
	private String lastExecutionStatus;
	private String jobClass;
	private String cronExpression;
	private String jobDescription;
	private String exceptionMessage;
	private String interfaceName;
	private Date lastStart;
	private Date lastStop;
	private Character cronJob;
	private Character enabled;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
