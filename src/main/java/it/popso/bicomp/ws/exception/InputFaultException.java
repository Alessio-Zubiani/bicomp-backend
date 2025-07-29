package it.popso.bicomp.ws.exception;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.popso.ws.common.testata.fault.schema.v11.InputFault;
import lombok.Getter;

@Getter

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class InputFaultException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	
	private final InputFault inputFault;

	public InputFaultException(String message, InputFault inputFault) {
		super(message);
		this.inputFault = inputFault;
	}
	
	public InputFaultException(String message, Throwable e, InputFault inputFault) {
		super(message, e);
		this.inputFault = inputFault;
	}
	
	public InputFaultException(InputFault inputFault) {
		this.inputFault = inputFault;
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
	    stream.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
	    stream.defaultReadObject();
	}

}
