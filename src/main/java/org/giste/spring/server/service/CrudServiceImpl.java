package org.giste.spring.server.service;

import org.giste.spring.server.entity.BaseEntity;
import org.giste.spring.server.repository.CrudRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation for a service with CRUD operations (Create,
 * Read, Update, Delete). Services of this type should subclass this one and
 * implement the abstract methods for mapping between the entity and the DTO.
 * 
 * @author Giste
 *
 * @param <DTO> {@link BaseDto} for entities managed by this service.
 * @param <ENT> {@link BaseEntity} managed by this service.
 */
public abstract class CrudServiceImpl<DTO extends BaseDto, ENT extends BaseEntity> extends BaseServiceImpl<DTO, ENT>
		implements CrudService<DTO> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Constructs a new CrudeService with the repository to use for managing the
	 * entity.
	 * 
	 * @param repository The repository to use to persist the entity.
	 */
	public CrudServiceImpl(CrudRepository<ENT> repository) {
		super(repository);
	}

	@Override
	public void delete(Long id) throws EntityNotFoundException {
		ENT entity = getSafeEntity(id);
		getRepository().delete(entity);

		LOGGER.debug("Deleted {}", entity);
	}

	@Override
	protected CrudRepository<ENT> getRepository() {
		return (CrudRepository<ENT>) super.getRepository();
	}

}