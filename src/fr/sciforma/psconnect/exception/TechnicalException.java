/*
 * © 2010 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.exception;

/**
 * exception thrown on a technical problem, serious problem like configuration,
 */
public class TechnicalException extends RuntimeException {

	private static final long serialVersionUID = 5327837082727322924L;

	/**
	 * constructor
	 * 
	 * @param throwable
	 * @param message
	 */
	public TechnicalException(Throwable throwable, String message) {
		super(message, throwable);
	}

	/**
	 * constructor
	 * 
	 * @param throwable
	 */
	public TechnicalException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * constructor
	 * 
	 * @param message
	 * @param throwable
	 */
	public TechnicalException(String message) {
		super(message);
	}

}
