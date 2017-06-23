package org.giste.spring.server.controller;

import org.giste.spring.server.service.CrudService;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Superclass for all the CRUD controllers. Provide methods to create, read,
 * update and delete the given entity. Entity has to be a subclass of
 * {@link NonRemovableDto}.
 * 
 * @author Giste
 *
 * @param <DTO> {@link NonRemovableDto} of the entity to be managed by the
 *            controller.
 */
public abstract class CrudRestController<DTO extends BaseDto> extends BaseRestController<DTO> {

	final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Constructs a new controller.
	 * 
	 * @param service Service to be used by the controller.
	 */
	public CrudRestController(CrudService<DTO> service) {
		super(service);
	}

	@Override
	protected CrudService<DTO> getService() {
		return (CrudService<DTO>) super.getService();
	}

	/**
	 * Deletes one entity.
	 * 
	 * @param id Identifier of the entity to delete.
	 * @return DTO with the values of the entity.
	 * @throws EntityNotFoundException If the entity to delete can't be found
	 */
	@DeleteMapping("/{id}")
	public void delete(@PathVariable("id") Long id) throws EntityNotFoundException {
		getService().delete(id);
	}

}