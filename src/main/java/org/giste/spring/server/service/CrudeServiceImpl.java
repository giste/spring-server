package org.giste.spring.server.service;

import org.giste.spring.server.entity.NonRemovableEntity;
import org.giste.spring.server.repository.CrudeRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.NonRemovableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation for a service with CRUDE operations (Create,
 * Read, Update, Disable, Enable). Services of this type should subclass this
 * one and implement the abstract methods for mapping between the entity and the
 * DTO.
 * 
 * @author Giste
 *
 * @param <DTO> {@link NonRemovableDto} for entities managed by this service.
 * @param <ENT> {@link NonRemovableEntity} managed by this service.
 */
public abstract class CrudeServiceImpl<DTO extends NonRemovableDto, ENT extends NonRemovableEntity>
		extends BaseServiceImpl<DTO, ENT>
		implements CrudeService<DTO> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Constructs a new CrudeService with the repository to use for managing the
	 * entity.
	 * 
	 * @param repository The repository to use to persist the entity.
	 */
	public CrudeServiceImpl(CrudeRepository<ENT> repository) {
		super(repository);
	}

	@Override
	public DTO enable(Long id) throws EntityNotFoundException {
		ENT entity = getSafeEntity(id);
		entity.setEnabled(true);
		ENT savedEntity = getRepository().save(entity);

		LOGGER.debug("Enabled entity {}", savedEntity);

		return getDtoFromEntity(savedEntity);
	}

	@Override
	public DTO disable(Long id) throws EntityNotFoundException {
		ENT entity = getSafeEntity(id);
		entity.setEnabled(false);
		ENT savedEntity = getRepository().save(entity);

		LOGGER.debug("Disabled entity {}", savedEntity);

		return getDtoFromEntity(savedEntity);
	}

	@Override
	protected CrudeRepository<ENT> getRepository() {
		return (CrudeRepository<ENT>) super.getRepository();
	}

}