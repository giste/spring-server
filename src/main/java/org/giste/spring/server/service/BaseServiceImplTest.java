package org.giste.spring.server.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.giste.spring.server.entity.BaseEntity;
import org.giste.spring.server.repository.BaseRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Base abstract class for testing CRUD services. It performs common tests for
 * this kind of services.
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
 * the DTO matches the corresponding ones in the entity. Default implementation
 * checks id property.</li>
 * </ul>
 * 
 * @author Giste
 *
 */
public abstract class BaseServiceImplTest<DTO extends BaseDto, ENT extends BaseEntity> {

	/**
	 * Gets the mocked repository to be used by service under testing. This
	 * method is called during setup.
	 * 
	 * @return The repository.
	 */
	protected abstract BaseRepository<ENT> getRepositoryMock();

	/**
	 * Gets the service under testing. This method is called during setup.
	 * 
	 * @return The service under testing.
	 */
	protected abstract BaseServiceImpl<DTO, ENT> getTestService();

	/**
	 * Gets an entity used for testing. The returned entity has to be a valid
	 * one.
	 * 
	 * @return The entity to use for testing.
	 */
	protected abstract ENT getNewEntity();

	/**
	 * Gets the type of the entity managed by this service.
	 * 
	 * @return The type of the entity managed by this service.
	 */
	protected abstract Class<ENT> getEntityType();

	/**
	 * Checks that properties of given DTO matches the properties in a target
	 * entity. Base implementation checks for <code>id</code> property. Override
	 * this method in subclasses for checking other properties.
	 * 
	 * @param dto The DTO to check.
	 * @param entity The entity to check with.
	 */
	protected void checkProperties(DTO dto, ENT entity) {
		assertThat(dto.getId(), is(entity.getId()));
	}

	/**
	 * Tests the <code>findAll()</code> method performing the following checks:
	 * <ul>
	 * <li>The identifier passed to repository matches the identifier passed to
	 * service.</li>
	 * <li><code>repository.findAll()</code> method is called by service.</li>
	 * <li>The DTO list returned by service has the right size.</li>
	 * <li>Every DTO in the list matches the respective testing entity.</li>
	 * </ul>
	 */
	@Test
	public void findAllIsValid() {
		ENT entity1 = getNewEntity();
		entity1.setId(1L);
		ENT entity2 = getNewEntity();
		entity2.setId(2L);

		List<ENT> entityList = new ArrayList<ENT>();
		entityList.add(entity1);
		entityList.add(entity2);
		when(getRepositoryMock().findAll()).thenReturn(entityList);

		List<DTO> readList = getTestService().findAll();

		verify(getRepositoryMock()).findAll();
		verifyNoMoreInteractions(getRepositoryMock());
		assertThat(readList.size(), is(entityList.size()));

		for (int i = 0; i < readList.size(); i++) {
			DTO dto = readList.get(i);
			ENT entity = entityList.get(i);
			checkProperties(dto, entity);
		}
	}

	/**
	 * Check that findAll() method returns an empty DTO list if there are no
	 * entities.
	 */
	@Test
	public void findAllIsEmpty() {
		List<ENT> entityList = new ArrayList<ENT>();
		when(getRepositoryMock().findAll()).thenReturn(entityList);

		List<DTO> readList = getTestService().findAll();

		verify(getRepositoryMock()).findAll();
		verifyNoMoreInteractions(getRepositoryMock());
		assertThat(readList.size(), is(0));
	}

	/**
	 * Tests the <code>findById()</code> method performing the following checks:
	 * <ul>
	 * <li>The identifier passed to repository matches the identifier passed to
	 * service.</li>
	 * <li><code>repository.findOne()</code> method is called by service.</li>
	 * <li>The DTO returned from service matches the entity returned by
	 * repository.</li>
	 * </ul>
	 */
	@Test
	public void findByIdIsValid() {
		ENT entity = getNewEntity();
		when(getRepositoryMock().findOne(entity.getId())).thenReturn(Optional.of(entity));

		DTO dto = getTestService().findById(entity.getId());

		verify(getRepositoryMock()).findOne(entity.getId());
		verifyNoMoreInteractions(getRepositoryMock());

		checkProperties(dto, entity);
	}

	/**
	 * Checks that <code>findById()</code> throws
	 * {@link EntityNotFoundException} when entity is not found.
	 */
	@Test
	public void findByIdEntityNotFound() {
		final Long id = 1L;

		when(getRepositoryMock().findOne(id)).thenReturn(Optional.ofNullable(null));

		try {
			getTestService().findById(id);

			fail("Expected EntityNotFoundException");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}
	}

	/**
	 * Tests the <code>create()</code> method performing the following checks:
	 * <ul>
	 * <li>The entity passed to repository matches the DTO passed to
	 * service.</li>
	 * <li>The DTO returned from service matches the entity returned by
	 * repository.</li>
	 * <li><code>repository.save()</code> method is called by service.</li>
	 * </ul>
	 */
	@Test
	public void createIsOk() {
		// Get entity from subclass and corresponding DTO from service.
		ENT entity = getNewEntity();
		DTO dto = getTestService().getDtoFromEntity(entity);

		when(getRepositoryMock().save(any(getEntityType()))).thenReturn(entity);

		DTO readDto = getTestService().create(dto);

		// Verify call to repository.create().
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(getEntityType());
		verify(getRepositoryMock()).save(entityCaptor.capture());
		verifyNoMoreInteractions(getRepositoryMock());

		// Check that entity passed to repository matches DTO passed to service.
		ENT capturedEntity = entityCaptor.getValue();
		checkProperties(dto, capturedEntity);

		// Check that read DTO matches entity returned by repository.
		checkProperties(readDto, entity);
	}

	/**
	 * Tests the <code>update()</code> method performing the following checks:
	 * <ul>
	 * <li>The entity passed to repository matches the DTO passed to
	 * service.</li>
	 * <li>The returned DTO from update() matches the entity returned by
	 * repository.</li>
	 * <li>finById() method is called in order to retrieve the most recent
	 * entity.</li>
	 * <li>save() method is called in the repository.</li>
	 * </ul>
	 */
	@Test
	public void updateIsValid() {
		// Get entity from subclass and corresponding DTO from service.
		ENT entity = getNewEntity();
		DTO dto = getTestService().getDtoFromEntity(entity);

		when(getRepositoryMock().findOne(dto.getId())).thenReturn(Optional.of(entity));
		when(getRepositoryMock().save(any(getEntityType()))).thenReturn(entity);

		DTO readDto = getTestService().update(dto);

		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(getEntityType());
		verify(getRepositoryMock()).findOne(dto.getId());
		verify(getRepositoryMock()).save(entityCaptor.capture());
		verifyNoMoreInteractions(getRepositoryMock());

		// Check that entity passed to repository matches DTO passed to service.
		ENT capturedEntity = entityCaptor.getValue();
		checkProperties(dto, capturedEntity);

		// Check that read DTO matches entity returned by repository.
		checkProperties(readDto, entity);
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown if the entity to
	 * update does not exist.
	 */
	@Test
	public void updateEntityNotFound() {
		ENT entity = getNewEntity();
		DTO dto = getTestService().getDtoFromEntity(entity);
		when(getRepositoryMock().findOne(dto.getId())).thenReturn(Optional.ofNullable(null));

		try {
			getTestService().update(dto);
			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(dto.getId()));
		}

		verify(getRepositoryMock()).findOne(dto.getId());
		verifyNoMoreInteractions(getRepositoryMock());
	}

}
