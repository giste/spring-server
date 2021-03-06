package org.giste.spring.server.controller;

import java.util.List;

import javax.validation.Valid;

import org.giste.spring.server.service.BaseService;
import org.giste.spring.server.service.exception.DuplicatedPropertyException;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Superclass for base REST controllers. Provide methods to create, read and
 * update the managed entities.
 * 
 * @author Giste
 *
 * @param <DTO> {@link BaseDto} of the entity to be managed by the controller.
 */
public abstract class BaseRestController<DTO extends BaseDto> {

	final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private final BaseService<DTO> service;

	/**
	 * Constructs a new controller.
	 * 
	 * @param service Service to be used by the controller.
	 */
	public BaseRestController(BaseService<DTO> service) {
		this.service = service;
	}

	/**
	 * Gets the service used by the controller to manage the entities.
	 * 
	 * @return The service used by the controller.
	 */
	protected BaseService<DTO> getService() {
		return service;
	}

	/**
	 * Creates a new Entity.
	 * 
	 * @param dto DTO with values for new entity.
	 * @return DTO with the values of the new created entity.
	 */
	@PostMapping
	public DTO create(@RequestBody @Valid final DTO dto) {
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
	 * @return DTO with the data of the requested entity.
	 * @throws EntityNotFoundException If the requested entity can't be found.
	 */
	@GetMapping(value = "/{id}")
	public DTO findById(@PathVariable("id") Long id) throws EntityNotFoundException {
		return service.findById(id);
	}

	/**
	 * Retrieves all existing entities.
	 * 
	 * @return List populated with the {@link NonRemovableDto} of all existent
	 *         entities.
	 */
	@GetMapping
	public List<DTO> findAll() {
		return service.findAll();
	}

	/**
	 * Updates one entity.
	 * 
	 * @param id Identifier of the entity to be updated.
	 * @param dto DTO with the values of the entity to update.
	 * @return DTO with the updated values of the entity.
	 * @throws EntityNotFoundException If the entity to update can't be found.
	 * @throws HttpRequestMethodNotSupportedException
	 */
	@PutMapping(value = "/{id}")
	public DTO update(@PathVariable("id") Long id, @RequestBody @Valid final DTO dto)
			throws EntityNotFoundException, HttpRequestMethodNotSupportedException {

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
	 * Gets the exception to be thrown if a duplicated property is found when
	 * creating or updating.
	 * 
	 * @param dto The DTO that caused the exception.
	 * @return The exception to be thrown.
	 */
	protected abstract DuplicatedPropertyException getDuplicatedPropertyException(DTO dto);
}