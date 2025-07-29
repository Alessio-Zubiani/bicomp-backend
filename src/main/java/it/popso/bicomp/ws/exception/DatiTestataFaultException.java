package it.popso.bicomp.ws.exception;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.popso.ws.common.testata.fault.schema.v11.DatiTestataFault;
import lombok.Getter;

@Getter

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class DatiTestataFaultException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	
	private final DatiTestataFault datiTestataFault;

	public DatiTestataFaultException(String message, DatiTestataFault datiTestataFault) {
		super(message);
		this.datiTestataFault = datiTestataFault;
	}
	
	public DatiTestataFaultException(String message, Throwable e, DatiTestataFault datiTestataFault) {
		super(message, e);
		this.datiTestataFault = datiTestataFault;
	}
	
	public DatiTestataFaultException(DatiTestataFault datiTestataFault) {
		this.datiTestataFault = datiTestataFault;
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
	    stream.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
	    stream.defaultReadObject();
	}

}
