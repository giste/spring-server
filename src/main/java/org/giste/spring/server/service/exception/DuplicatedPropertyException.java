package org.giste.spring.server.service.exception;

/**
 * Exception thrown when a create or update action is not possible due to some
 * duplicated property not allowed. Usually is produced by unique constraints in
 * that property.
 * 
 * @author Giste
 */
public class DuplicatedPropertyException extends RestException {

	private static final long serialVersionUID = 2478951935047677057L;

	/**
	 * Creates a DuplicatedPropertyException with an error code, a message and
	 * developer info.
	 * 
	 * @param code The error code for this REST exception.
	 * @param message The detail message. The detail message is saved for later
	 *            retrieval by the getMessage() method.
	 * @param developerInfo Detailed information for developers.
	 */
	public DuplicatedPropertyException(String code, String message, String developerInfo) {
		super(code, message, developerInfo);
	}

}
