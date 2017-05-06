package org.giste.spring.server.service;

import java.util.List;

import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.NonRemovableDto;

/**
 * Service interface for CRUDE operations (Create, Remove, Update, Disable,
 * Enable) with an entity. It accepts DTO parameters that are converted to
 * entities inside the service. Service uses entities to call repository
 * methods.
 * 
 * @author Giste
 *
 * @param <T> DTO of the entity to manage.
 */
public interface CrudeService<T extends NonRemovableDto> {

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
	 * Enables one entity in the application.
	 * 
	 * @param id Identifier of the entity to be enabled.
	 * @return {@link entityDto} with the values of the enabled entity.
	 * @throws entityNotFoundException If the entity to enable does not exist.
	 */
	T enable(Long id) throws EntityNotFoundException;

	/**
	 * Disables one entity in the application.
	 * 
	 * @param id Identifier of the entity to be disabled.
	 * @return {@link entityDto} with the values of the disabled entity.
	 * @throws entityNotFoundException If the entity to disable does not exist.
	 */
	T disable(Long id) throws EntityNotFoundException;

}