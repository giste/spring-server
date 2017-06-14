package org.giste.spring.server.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.giste.spring.server.entity.BaseEntity;
import org.giste.spring.server.entity.NonRemovableEntity;
import org.giste.spring.server.repository.CrudRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Base abstract class for testing CRUD services. It performs common tests for
 * this type of services.
 * 
 * Subclasses has to implement the following methods:
 * <ul>
 * <li>{@link #getRepositoryMock()} to get the mock of the repository that the
 * service to test is going to use.</li>
 * <li>{@link #getTestService()} to get the service under test.</li>
 * <li>{@link #getNewEntity()} to get entities for testing.</li>
 * <li>{@link #getEntityType()} to get the type of the entity managed by the
 * service under testing.</li>
 * </ul>
 * 
 * Subclasses should override the following methods.
 * <ul>
 * <li>{@link #checkProperties(BaseDto, BaseEntity)} to assert that fields in
 * the DTO matches the corresponding ones in the entity. Implementation from
 * this class checks <code>id</code> property.</li>
 * <li>{@link #checkProperties(NonRemovableEntity, NonRemovableEntity)} to
 * assert that the two entities matches. Implementation from this class checks
 * <code>id</code> property.</li>
 * </ul>
 * 
 * @author Giste
 *
 * @param <DTO> BaseDto associated with the entity managed by the service under
 *            testing.
 * @param <ENT> BaseEntity managed by the service under testing.
 */
public abstract class CrudServiceImplTest<DTO extends BaseDto, ENT extends BaseEntity>
		extends BaseServiceImplTest<DTO, ENT> {

	@Override
	protected abstract CrudServiceImpl<DTO, ENT> getTestService();

	@Override
	protected abstract CrudRepository<ENT> getRepositoryMock();

	/**
	 * Checks that <code>id</code> property matches in two entities. Override
	 * this method in subclasses for checking other properties.
	 * 
	 * @param entity The entity to check.
	 * @param target The target entity to check.
	 */
	protected void checkProperties(ENT entity, ENT target) {
		assertThat(entity.getId(), is(target.getId()));
	}

	/**
	 * Tests the <code>delete()</code> method performing the following checks:
	 * <ul>
	 * <li><code>repository.findOne()</code> method is called by service to get
	 * the most recent entity to delete.</li>
	 * <li>The identifier passed to repository matches the identifier passed to
	 * service.</li>
	 * <li><code>repository.delete()</code> method is called by service.</li>
	 * <li>The entity passed to <code>delete()</code> method is the one returned
	 * by <code>findOne()</code> method.</li>
	 * </ul>
	 */
	@Test
	public void deleteIsValid() {
		ENT entity = getNewEntity();

		when(getRepositoryMock().findOne(entity.getId())).thenReturn(Optional.of(entity));

		getTestService().delete(entity.getId());

		verify(getRepositoryMock()).findOne(entity.getId());
		verify(getRepositoryMock()).delete(any(getEntityType()));
		verifyNoMoreInteractions(getRepositoryMock());

		// Check repository calls.
		verify(getRepositoryMock()).findOne(entity.getId());
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(getEntityType());
		verify(getRepositoryMock()).delete(entityCaptor.capture());
		verifyNoMoreInteractions(getRepositoryMock());

		// Check entity passed to delete() method.
		ENT capturedEntity = entityCaptor.getValue();
		checkProperties(capturedEntity, entity);
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown when the entity to
	 * delete does not exist.
	 */
	@Test
	public void deleteEntityNotFound() {
		Long id = 1L;

		when(getRepositoryMock().findOne(id)).thenReturn(Optional.ofNullable(null));

		try {
			getTestService().delete(id);
			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}

		verify(getRepositoryMock()).findOne(id);
		verifyNoMoreInteractions(getRepositoryMock());
	}

}
