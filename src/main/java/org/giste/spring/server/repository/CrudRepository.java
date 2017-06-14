package org.giste.spring.server.repository;

import org.giste.spring.server.entity.BaseEntity;

/**
 * Base repository for CRUD (Create, Read, Update, Delete) operations.
 * 
 * @author Giste
 *
 * @param <T> BaseEntity to manage.
 */
public interface CrudRepository<T extends BaseEntity> extends BaseRepository<T> {

	/**
	 * Deletes one entity.
	 * 
	 * @param entity The entity to delete.
	 */
	void delete(T entity);
}
