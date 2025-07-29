package it.popso.bicomp.ws.exception;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.popso.ws.common.testata.fault.schema.v11.ApplicationFault;
import lombok.Getter;

@Getter

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class ApplicationFaultException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	
	private final ApplicationFault applicationFault;

	public ApplicationFaultException(String message, ApplicationFault applicationFault) {
		super(message);
		this.applicationFault = applicationFault;
	}
	
	public ApplicationFaultException(String message, Throwable e, ApplicationFault applicationFault) {
		super(message, e);
		this.applicationFault = applicationFault;
	}
	
	public ApplicationFaultException(ApplicationFault applicationFault) {
		this.applicationFault = applicationFault;
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
	    stream.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
	    stream.defaultReadObject();
	}
	
}
