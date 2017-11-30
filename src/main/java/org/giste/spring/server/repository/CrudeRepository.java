package org.giste.spring.server.repository;

import org.giste.spring.server.entity.NonRemovableEntity;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository for CRUDE (Create, Read, Update, Disable, Enable) operations.
 * 
 * @author Giste
 *
 * @param <T> NonRemovableEntity to manage.
 */
@NoRepositoryBean
public interface CrudeRepository<T extends NonRemovableEntity> extends BaseRepository<T> {

	// TODO Return only enabled entities
	Iterable<T> findAllByEnabledIsTrue();
}
