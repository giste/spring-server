package org.giste.spring.server.service;

import java.util.List;

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
public interface CrudService<T extends BaseDto> {

	/**
	 * Creates a new entity.
	 * 
	 * @param dto DTO with the values for the new entity.
	 * @return DTO with the values of the created entity.
	 */
	T create(T dto);

	/**
	 * Retrieves one entity by its identifier.
	 * 
	 * @param id Identifier of the entity to find.
	 * @return DTO with the values of the found entity.
	 */
	T findById(Long id) throws EntityNotFoundException;

	/**
	 * Retrieves all entities.
	 * 
	 * @return List populated with the DTO for each entity.
	 */
	List<T> findAll();

	/**
	 * Updates the values of one entity.
	 * 
	 * @param dto DTO with the values of the entity to update.
	 * @return DTO with the updated values of the entity.
	 */
	T update(T dto) throws EntityNotFoundException;

	/**
	 * Deletes one entity in the application.
	 * 
	 * @param id identifier of the entity to delete.
	 * @throws entityNotFoundException If the entity to delete does not exist.
	 */
	void delete(Long id) throws EntityNotFoundException;

}