/*
 * © 2008 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fr.sciforma.psconnect.exception.WarningException;

/**
 * warnings mechanism
 */
public class ServiceResponse<T> implements Serializable {

	private static final long serialVersionUID = -6192195136858464852L;

	private String message;

	private WarningException warningException;

	private List<Information> informations = new LinkedList<Information>();

	private T returnValue;

	private class Information implements Serializable {

		private static final long serialVersionUID = 1L;

		private String message;

		private Throwable cause;

		public Information(String message) {
			this.message = message;
		}

		public Information(Throwable cause, String message) {
			this.message = message;
			this.cause = cause;
		}

		public String getMessage() {
			return message;
		}

		public Throwable getCause() {
			return cause;
		}
	}

	public ServiceResponse(String message) {
		this.message = message;
		this.warningException = new WarningException(message, true);
	}

	public ServiceResponse(String message, boolean ignoreWarning) {
		this.message = message;
		this.warningException = new WarningException(message, ignoreWarning);
	}

	public ServiceResponse(String message, WarningException warningException) {
		this.message = message;
		this.warningException = warningException;
	}

	public void addInformation(String message) {
		informations.add(new Information(message));
	}

	public void addInformation(String message, Throwable cause) {
		informations.add(new Information(cause, message));
	}

	public boolean hasInformation() {
		return !this.informations.isEmpty();
	}

	public List<Information> getInformations() {
		return Collections.unmodifiableList(informations);
	}

	public String getMessage() {
		StringBuilder stringBuilder = new StringBuilder(this.message);
		for (Information information : this.informations) {
			stringBuilder.append(information.getMessage());
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

	public String[] getMessages() {
		String[] messages = new String[this.informations.size()];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = informations.get(i).getMessage();
		}
		return messages;
	}

	public WarningException getWarningException() {
		return warningException;
	}

	public void setReturnValue(T returnValue) {
		this.returnValue = returnValue;
	}

	public ServiceResponse<T> withReturnValue(T returnValue) {
		this.returnValue = returnValue;
		return this;
	}

	public T getReturnValue() {
		return returnValue;
	}

}
