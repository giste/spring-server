package org.giste.spring.server.service.exception;

/**
 * Base REST exception. It contains the error code and developer Info.
 * 
 * @author Giste
 */
public class RestException extends RuntimeException {

	private static final long serialVersionUID = 1429754664986546753L;

	private String code;
	private String developerInfo;

	/**
	 * Creates a REST exception with an error code, a message and developer
	 * info.
	 * 
	 * @param code The error code for this REST exception.
	 * @param message The detail message. The detail message is saved for later
	 *            retrieval by the getMessage() method.
	 * @param developerInfo Detailed information for developers.
	 */
	public RestException(String code, String message, String developerInfo) {
		super(message);
		this.code = code;
		this.developerInfo = developerInfo;
	}

	/**
	 * Creates a REST exception with an error code, a message, developer info
	 * and a cause.
	 * 
	 * @param code The error code for this REST exception.
	 * @param message The detail message. The detail message is saved for later
	 *            retrieval by the getMessage() method.
	 * @param developerInfo Detailed information for developers.
	 * @param cause the cause (which is saved for later retrieval by the
	 *            getCause() method). (A null value is permitted, and indicates
	 *            that the cause is nonexistent or unknown.)
	 */
	public RestException(String code, String message, String developerInfo, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.developerInfo = developerInfo;
	}

	/**
	 * Gets the code error for this exception.
	 * 
	 * @return The error code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Gets the developer info for this exception.
	 * 
	 * @return The developer info.
	 */
	public String getDeveloperInfo() {
		return developerInfo;
	}

}
