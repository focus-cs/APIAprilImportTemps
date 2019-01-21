/*
 * © 2011 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.exception;

/**
 * Base des exceptions métier
 */
public class BusinessException extends Exception {

	private static final long serialVersionUID = -5980477211664716473L;

	private Enum<?> errorCode;

	/**
	 * default constructor
	 * 
	 * @param message
	 */
	public BusinessException(String message) {
		super(message);
	}

	/**
	 * Constructeur complet
	 * 
	 * @param errorCode
	 *            cause
	 * @param message
	 *            expliquant l'erreur
	 */
	public <E extends Enum<?>> BusinessException(E errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Simple constructeur sans exception cause
	 * 
	 * @param message
	 *            expliquant l'erreur
	 */
	public <E extends Enum<?>> BusinessException(Exception e, E errorCode,
			String message) {
		super(message, e);
		this.errorCode = errorCode;
	}

	@Override
	public String getMessage() {
		return errorCode == null ? super.getMessage() : errorCode.name()
				+ " - " + super.getMessage();
	}
}