package org.giste.spring.server.repository;

import java.util.Optional;

import org.giste.spring.server.entity.BaseEntity;
import org.springframework.data.repository.Repository;

/**
 * Base repository for CRUD (Create, Read, Update, Delete) and CRUDE (Create,
 * Read, Update, Disable, Enable) operations.
 * 
 * @author Giste
 *
 * @param <T> BaseEntity to manage.
 */
public interface BaseRepository<T extends BaseEntity> extends Repository<T, Long> {

	/**
	 * Gets one entity given its identifier.
	 * 
	 * @param id The identifier.
	 * @return The found entity or null.
	 */
	Optional<T> findOne(Long id);

	/**
	 * Finds all entities.
	 * 
	 * @return Iterable with all existing entities.
	 */
	Iterable<T> findAll();

	/**
	 * Saves (creates or updates) one entity.
	 * 
	 * @param entity The entity to save.
	 * @return The saved entity.
	 */
	T save(T entity);

}
