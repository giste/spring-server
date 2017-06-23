package org.giste.spring.server.controller;

import org.giste.spring.server.entity.NonRemovableEntity;
import org.giste.spring.server.service.CrudeService;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Superclass for all the CRUDE controllers. Provide methods to create, read,
 * update, disable and enable the given entity. Entity has to be a subclass of
 * {@link NonRemovableDto}.
 * 
 * @author Giste
 *
 * @param <DTO> {@link NonRemovableDto} of the entity to be managed by the
 *            controller.
 */
public abstract class CrudeRestController<DTO extends NonRemovableDto> extends BaseRestController<DTO> {

	final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Constructs a controller to manage {@link NonRemovableEntity}. Controller
	 * takes {@link NonRemovableDto} as parameters and return values.
	 * 
	 * @param service {@link CrudeService) used by controller to manage
	 *            entities.
	 */
	public CrudeRestController(CrudeService<DTO> service) {
		super(service);
	}

	@Override
	protected CrudeService<DTO> getService() {
		return (CrudeService<DTO>) super.getService();
	}

	/**
	 * Enables one entity in the application.
	 * 
	 * @param id Identifier of the entity to enable.
	 * @return {@link NonRemovableDto} with the values of the enabled entity.
	 * @throws EntityNotFoundException If the entity to enable can't be found.
	 */
	@PutMapping("/{id}/enable")
	public DTO enable(@PathVariable("id") Long id) throws EntityNotFoundException {
		return getService().enable(id);
	}

	/**
	 * Disables one entity in the application.
	 * 
	 * @param id Identifier of the entity to disable.
	 * @return {@link BaseDto} with the values of the disabled entity.
	 * @throws EntityNotFoundException If the entity to disable can't be found
	 */
	@PutMapping("/{id}/disable")
	public DTO disable(@PathVariable("id") Long id) throws EntityNotFoundException {
		return getService().disable(id);
	}

}