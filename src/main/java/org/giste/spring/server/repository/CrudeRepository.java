package org.giste.spring.server.repository;

import org.giste.spring.server.entity.NonRemovableEntity;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * Base repository for CRUDE (Create, Read Update, Disable, Enable) operations.
 * 
 * @author Giste
 *
 * @param <T> NonRemovableEntity to manage.
 */
@NoRepositoryBean
public interface CrudeRepository<T extends NonRemovableEntity> extends Repository<T, Long> {

	/**
	 * Gets one entity given its identifier.
	 * 
	 * @param id The identifier.
	 * @return The found entity or null.
	 */
	T findOne(Long id);

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
