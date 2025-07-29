package it.popso.bicomp.model;

import java.io.Serializable;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the TIMER database table.
 * 
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name="TIMER", uniqueConstraints = {
	@UniqueConstraint(name = "TIMER_JOB_NAME_IDX", columnNames = {"JOB_NAME"}),
	@UniqueConstraint(name = "TIMER_JOB_CLASS_IDX", columnNames = {"JOB_CLASS"})
})
public class Timer implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="TIMER_JOBID_GENERATOR", sequenceName="TIMER_SEQUENCE", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TIMER_JOBID_GENERATOR")
	@Column(name="JOB_ID")
	private BigDecimal jobId;

	@Column(name="CRON_EXPRESSION")
	private String cronExpression;

	@Column(name="CRON_JOB")
	private Character cronJob;

	@Column(name = "JOB_DESCRIPTION")
	private String jobDescription;

	@Column(name = "ENABLED")
	private Character enabled;

	@Column(name="INTERFACE_NAME")
	private String interfaceName;

	@Column(name="JOB_CLASS")
	private String jobClass;

	@Column(name="JOB_GROUP")
	private String jobGroup;

	@Column(name="JOB_NAME")
	private String jobName;

	@Enumerated(EnumType.STRING)
	@Column(name="JOB_STATUS")
	private TimerStatusEnum jobStatus;

	@Column(name="LAST_EXECUTION_STATUS")
	private String lastExecutionStatus;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_START")
	private Date lastStart;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_STOP")
	private Date lastStop;

}