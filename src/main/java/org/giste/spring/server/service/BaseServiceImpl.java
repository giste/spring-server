package org.giste.spring.server.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.giste.spring.server.entity.BaseEntity;
import org.giste.spring.server.entity.NonRemovableEntity;
import org.giste.spring.server.repository.BaseRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
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
public abstract class BaseServiceImpl<DTO extends BaseDto, ENT extends BaseEntity>
		implements BaseService<DTO> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private BaseRepository<ENT> repository;

	/**
	 * Constructs a new CrudeService with the repository to use for managing the
	 * entity.
	 * 
	 * @param repository The repository to persist the entities.
	 */
	public BaseServiceImpl(BaseRepository<ENT> repository) {
		this.repository = repository;
	}

	@Override
	public DTO create(DTO dto) {
		ENT entity = getEntityFromDto(dto);
		ENT savedEntity = repository.save(entity);

		LOGGER.debug("Created {}", savedEntity);

		return getDtoFromEntity(savedEntity);
	}

	@Override
	public DTO findById(Long id) throws EntityNotFoundException {
		ENT entity = getSafeEntity(id);

		LOGGER.debug("Found {}", entity);

		return getDtoFromEntity(entity);
	}

	@Override
	public List<DTO> findAll() {
		return StreamSupport.stream(repository.findAll().spliterator(), false)
				.map(entity -> getDtoFromEntity(entity))
				.collect(Collectors.toList());
	}

	@Override
	public DTO update(DTO dto) throws EntityNotFoundException {
		ENT entity = getSafeEntity(dto.getId());
		entity = updateEntityFromDto(entity, dto);
		ENT savedEntity = repository.save(entity);

		LOGGER.debug("Updated {}", savedEntity);

		return getDtoFromEntity(savedEntity);
	}

	/**
	 * Tries to get a single entity from its identifier. Throws
	 * {@link EntityNotFoundException} if the entity can't be found.
	 * 
	 * @param id Identifier of the entity to find.
	 * @return The found entity.
	 * @throws EntityNotFoundException If the entity can't be found.
	 */
	protected ENT getSafeEntity(Long id) throws EntityNotFoundException {
		Optional<ENT> entity = repository.findOne(id);

		if (entity.isPresent()) {
			return entity.get();
		} else {
			LOGGER.debug("Throwing EntityNotFoundException {}", id);

			throw getEntityNotFoundException(id);
		}
	}

	/**
	 * Gets a {@link NonRemovableEntity} from a given {@link NonRemovableDto}.
	 * 
	 * @param dto {@link NonRemovableDto} for getting the entity.
	 * @return The entity.
	 */
	protected abstract ENT getEntityFromDto(DTO dto);

	/**
	 * Gets a {@link NonRemovableDto} from a given {@link NonRemovableEntity}.
	 * 
	 * @param entity {@link NonRemovableEntity} for getting the DTO.
	 * @return The {@link NonRemovableDto}.
	 */
	protected abstract DTO getDtoFromEntity(ENT entity);

	/**
	 * Updates a given {@link NonRemovableEntity} with the values from a
	 * {@link NonRemovableDto}.
	 * 
	 * @param entity The {@link NonRemovableEntity} to update.
	 * @param dto The {@link NonRemovableDto} with the values for updating the
	 *            entity.
	 * @return The updated {@link NonRemovableEntity}
	 */
	protected abstract ENT updateEntityFromDto(ENT entity, DTO dto);

	/**
	 * Gets the {@link EntityNotFoundException} to be thrown when a looked up
	 * entity is not found, filled with information from the subclass .
	 * 
	 * @param id Identifier of the entity not found.
	 * @return The {@link EntityNotFoundException} to be thrown.
	 */
	protected abstract EntityNotFoundException getEntityNotFoundException(Long id);

	/**
	 * Gets the repository used by this service.
	 * 
	 * @return The repository used by this service.
	 */
	protected BaseRepository<ENT> getRepository() {
		return repository;
	}

}