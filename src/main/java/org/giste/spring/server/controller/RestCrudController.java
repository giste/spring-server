package org.giste.spring.server.controller;

import java.util.List;

import javax.validation.Valid;

import org.giste.spring.server.service.CrudService;
import org.giste.spring.server.service.exception.DuplicatedPropertyException;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Superclass for all the CRUD controllers. Provide methods to create, read,
 * update and delete the given entity. Entity has to be a subclass of
 * {@link NonRemovableDto}.
 * 
 * @author Giste
 *
 * @param <T> {@link NonRemovableDto} of the entity to be managed by the
 *            controller.
 */
public abstract class RestCrudController<T extends BaseDto> {

	final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected final CrudService<T> service;

	/**
	 * Constructs a new controller.
	 * 
	 * @param service Service to be used by the controller.
	 */
	public RestCrudController(CrudService<T> service) {
		this.service = service;
	}

	/**
	 * Creates a new Entity.
	 * 
	 * @param dto {@link NonRemovableDto} with values for new entity.
	 * @return {@link NonRemovableDto} with the values of the new created
	 *         entity.
	 */
	@PostMapping
	public T create(@RequestBody @Valid final T dto) {
		try {
			return service.create(dto);
		} catch (DataIntegrityViolationException e) {
			throw getDuplicatedPropertyException(dto);
		}
	}

	/**
	 * Retrieves one single entity given its identifier.
	 * 
	 * @param id Identifier of the entity to retrieve.
	 * @return {@link NonRemovableDto} with the data of the requested entity.
	 * @throws EntityNotFoundException If the requested entity can't be found.
	 */
	@GetMapping(value = "/{id}")
	public T findById(@PathVariable("id") Long id) throws EntityNotFoundException {
		return service.findById(id);
	}

	/**
	 * Retrieves all existing entities.
	 * 
	 * @return List populated with the {@link NonRemovableDto} of all existent
	 *         entities.
	 */
	@GetMapping
	public List<T> findAll() {
		return service.findAll();
	}

	/**
	 * Updates one entity.
	 * 
	 * @param id Identifier of the entity to be updated.
	 * @param dto {@link NonRemovableDto} with the values of the entity to
	 *            update.
	 * @return {@link NonRemovableDto} with the updated values of the entity.
	 * @throws EntityNotFoundException If the entity to update can't be found.
	 */
	@PutMapping(value = "/{id}")
	public T update(@PathVariable("id") Long id, @RequestBody @Valid final T dto) throws EntityNotFoundException {

		// If club identifier is different, overwrite it.
		if (id != dto.getId()) {
			LOGGER.debug("Identifier from Dto ({}) is different than identifier from URI ({})", dto.getId(), id);
			dto.setId(id);
		}

		try {
			return service.update(dto);
		} catch (DataIntegrityViolationException e) {
			throw getDuplicatedPropertyException(dto);
		}
	}

	/**
	 * Disables one entity in the application.
	 * 
	 * @param id Identifier of the entity to disable.
	 * @return {@link BaseDto} with the values of the disabled entity.
	 * @throws EntityNotFoundException If the entity to disable can't be found
	 */
	@DeleteMapping("/{id}")
	public void delete(@PathVariable("id") Long id) throws EntityNotFoundException {
		service.delete(id);
	}

	/**
	 * Gets the exception to be thrown if a duplicated property is found when
	 * creating or updating.
	 * 
	 * @param dto The DTO that caused the exception.
	 * @return The exception to be thrown.
	 */
	protected abstract DuplicatedPropertyException getDuplicatedPropertyException(T dto);
}