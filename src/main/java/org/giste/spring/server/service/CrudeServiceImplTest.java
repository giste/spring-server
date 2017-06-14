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
import org.giste.spring.server.repository.CrudeRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Base abstract class for testing CRUDE services. It performs common tests for
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
 * this class checks <code>id</code> and <code>enabled</code> properties.</li>
 * <li>{@link #checkProperties(NonRemovableEntity, NonRemovableEntity)} to
 * assert that the two entities matches. Implementation from this class checks
 * <code>id</code> and <code>enabled</code> properties.</li>
 * </ul>
 * 
 * @author Giste
 *
 * @param <DTO> NonRemovableDto associated with the entity managed by the
 *            service under testing.
 * @param <ENT> NonRemovableEntity managed by the service under testing.
 */
public abstract class CrudeServiceImplTest<DTO extends NonRemovableDto, ENT extends NonRemovableEntity>
		extends BaseServiceImplTest<DTO, ENT> {

	@Override
	protected abstract CrudeServiceImpl<DTO, ENT> getTestService();

	@Override
	protected abstract CrudeRepository<ENT> getRepositoryMock();

	/**
	 * Checks that <code>id</code> and <code>enabled</code> properties are equal
	 * in DTO and entity. Override this method in subclasses for checking other
	 * properties.
	 * 
	 * @param dto The DTO to check.
	 * @param entity The entity to check.
	 */
	@Override
	protected void checkProperties(DTO dto, ENT entity) {
		super.checkProperties(dto, entity);
		assertThat(dto.isEnabled(), is(entity.isEnabled()));
	}

	/**
	 * Checks that <code>id</code> and <code>enabled</code> properties matches
	 * in two entities. Override this method in subclasses for checking other
	 * properties.
	 * 
	 * @param entity The entity to check.
	 * @param target The target entity to check.
	 */
	protected void checkProperties(ENT entity, ENT target) {
		assertThat(entity.getId(), is(target.getId()));
		assertThat(entity.isEnabled(), is(target.isEnabled()));
	}

	/**
	 * Tests the <code>enable()</code> method performing the following checks:
	 * <ul>
	 * <li><code>repository.findOne()</code> method is called by service to get
	 * the most recent entity to enable.</li>
	 * <li>The identifier passed to repository matches the identifier passed to
	 * service.</li>
	 * <li><code>repository.save()</code> method is called by service.</li>
	 * <li>The entity passed to <code>save()</code> method is the one returned
	 * by <code>findOne()</code> method, but enabled.</li>
	 * <li>The returned DTO matches the entity returned by repository.</li>
	 * </ul>
	 */
	@Test
	public void enableIsValid() throws Exception {
		ENT disabledEntity = getNewEntity();
		disabledEntity.setEnabled(false);
		ENT enabledEntity = getNewEntity();
		enabledEntity.setEnabled(true);

		when(getRepositoryMock().findOne(disabledEntity.getId())).thenReturn(Optional.of(disabledEntity));
		when(getRepositoryMock().save(any(getEntityType()))).thenReturn(enabledEntity);

		DTO readDto = getTestService().enable(disabledEntity.getId());

		// Check repository calls.
		verify(getRepositoryMock()).findOne(disabledEntity.getId());
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(getEntityType());
		verify(getRepositoryMock()).save(entityCaptor.capture());
		verifyNoMoreInteractions(getRepositoryMock());

		// Check entity passed to save() method.
		ENT capturedEntity = entityCaptor.getValue();
		checkProperties(capturedEntity, enabledEntity);

		// Check DTO returned by service.
		checkProperties(readDto, enabledEntity);
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown when the entity to
	 * enable does not exist.
	 */
	@Test
	public void enableEntityNotFound() {
		final Long id = 1L;
		when(getRepositoryMock().findOne(id)).thenReturn(Optional.ofNullable(null));

		try {
			getTestService().enable(id);

			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}

		verify(getRepositoryMock()).findOne(id);
		verifyNoMoreInteractions(getRepositoryMock());
	}

	/**
	 * Tests the <code>disable()</code> method performing the following checks:
	 * <ul>
	 * <li><code>repository.findOne()</code> method is called by service to get
	 * the most recent entity to disable.</li>
	 * <li>The identifier passed to repository matches the identifier passed to
	 * service.</li>
	 * <li><code>repository.save()</code> method is called by service.</li>
	 * <li>The entity passed to <code>save()</code> method is the one returned
	 * by <code>findOne()</code> method, but disabled.</li>
	 * <li>The returned DTO matches the entity returned by repository.</li>
	 * </ul>
	 */
	@Test
	public void disableIsValid() {
		ENT enabledEntity = getNewEntity();
		enabledEntity.setEnabled(true);
		ENT disabledEntity = getNewEntity();
		disabledEntity.setEnabled(false);

		when(getRepositoryMock().findOne(enabledEntity.getId())).thenReturn(Optional.of(enabledEntity));
		when(getRepositoryMock().save(any(getEntityType()))).thenReturn(disabledEntity);

		DTO readDto = getTestService().disable(enabledEntity.getId());

		// Check repository calls.
		verify(getRepositoryMock()).findOne(enabledEntity.getId());
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(getEntityType());
		verify(getRepositoryMock()).save(entityCaptor.capture());

		// Check entity passed to save() method.
		ENT capturedEntity = entityCaptor.getValue();
		checkProperties(capturedEntity, disabledEntity);

		// Check DTO returned by service.
		checkProperties(readDto, disabledEntity);
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown when the entity to
	 * disable does not exist.
	 */
	@Test
	public void disableEntityNotFound() throws Exception {
		final Long id = 1L;
		when(getRepositoryMock().findOne(id)).thenReturn(Optional.ofNullable(null));

		try {
			getTestService().disable(id);

			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}

		verify(getRepositoryMock()).findOne(id);
		verifyNoMoreInteractions(getRepositoryMock());
	}

}
