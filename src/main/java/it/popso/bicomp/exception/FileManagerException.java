package it.popso.bicomp.exception;


public class FileManagerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FileManagerException(String message) {
		super(message);
	}

	public FileManagerException(Throwable cause) {
		super(cause);
	}

}
