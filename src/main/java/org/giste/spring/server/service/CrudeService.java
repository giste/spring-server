package org.giste.spring.server.service;

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
public interface CrudeService<T extends NonRemovableDto> extends BaseService<T> {

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