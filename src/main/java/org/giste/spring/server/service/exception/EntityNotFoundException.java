package org.giste.spring.server.service.exception;

/**
 * Exception thrown when a single entity is looked up by an unique identifier
 * and it's not found.
 * 
 * @author Giste
 */
public class EntityNotFoundException extends RestException {

	private static final long serialVersionUID = 629573734793213907L;
	
	private Object id;

	/**
	 * Creates a new EntityNotFoundException with an error code, a message and
	 * developer info.
	 * 
	 * @param id The identifier used for looking up the entity.
	 * @param code The error code for this REST exception.
	 * @param message The detail message. The detail message is saved for later
	 *            retrieval by the getMessage() method.
	 * @param developerInfo Detailed information for developers.
	 */
	public EntityNotFoundException(Object id, String code, String message, String developerInfo) {
		super(code, message, developerInfo);
		this.id = id;
	}

	/**
	 * Gets the identifier used for looking up the entity.
	 * 
	 * @return The identifier.
	 */
	public Object getId() {
		return id;
	}

}
