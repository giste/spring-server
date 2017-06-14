package org.giste.spring.server.service;

import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;

/**
 * Service interface for CRUD operations (Create, Read, Update, Delete) with an
 * entity. It accepts DTO parameters that are converted to entities inside the
 * service. Service uses entities to call repository methods.
 * 
 * @author Giste
 *
 * @param <T> DTO of the entity to manage.
 */
public interface CrudService<T extends BaseDto> extends BaseService<T> {

	/**
	 * Deletes one entity in the application.
	 * 
	 * @param id identifier of the entity to delete.
	 * @throws entityNotFoundException If the entity to delete does not exist.
	 */
	void delete(Long id) throws EntityNotFoundException;

}