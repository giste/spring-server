package org.giste.spring.server.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.giste.spring.server.entity.BaseEntity;
import org.giste.spring.server.repository.CrudRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Base abstract class for testing CRUD services. It performs common tests for
 * this kind of services.
 * 
 * Subclasses has to implement the following methods:
 * <ul>
 * <li>getRepositoryMock() to get the mock of the repository that the service to
 * test is going to use.
 * <li>getService() to get the service under test.
 * <li>getNewEntity() to get entities for testing.
 * </ul>
 * 
 * Subclasses should override the following methods.
 * <ul>
 * <li>checkFields() to assert that fields in the DTO matches the corresponding
 * ones in the entity. Default implementation checks id and enabled fields.
 * <li>verifyDuplicatedProperties() to verify that findBy...() methods are
 * called by service when checking for violations of unique constraints. Default
 * implementation does nothing.
 * </ul>
 * 
 * @author Giste
 *
 */
public abstract class CrudServiceImplTest<DTO extends BaseDto, ENT extends BaseEntity> {

	private CrudRepository<ENT> repository;

	private CrudServiceImpl<DTO, ENT> service;

	private Class<ENT> entityType;

	@Before
	public void setup() {
		repository = getRepositoryMock();
		service = getTestService();

		this.entityType = getEntityType();
	}

	/**
	 * Gets the mocked repository to be used by service under testing. This
	 * method is called during setup.
	 * 
	 * @return The repository.
	 */
	protected abstract CrudRepository<ENT> getRepositoryMock();

	/**
	 * Gets the service under testing. This method is called during setup.
	 * 
	 * @return The service under testing.
	 */
	protected abstract CrudServiceImpl<DTO, ENT> getTestService();

	/**
	 * Gets an entity used for testing. Tests in this class can modify the
	 * identifier and the enabled state of the entity.
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
	 * Checks that <code>id</code> and <code>enabled</code> properties are equal
	 * in DTO and entity. Override this method in subclasses for checking other
	 * properties.
	 * 
	 * @param dto The DTO to check.
	 * @param entity The entity to check.
	 */
	protected void checkFields(DTO dto, ENT entity) {
		assertThat(dto.getId(), is(entity.getId()));
	}

	/**
	 * Test for findAll() method. It asks subclass for two entities, changes
	 * their identifiers to 1L and 2L respectively and put them in a list.
	 * Checks that the returned DTO list has two items and call subclass
	 * checkFields method to compare each DTO in the returned list with the
	 * corresponding entity.
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
		when(repository.findAll()).thenReturn(entityList);

		List<DTO> readList = service.findAll();

		verify(repository).findAll();
		verifyNoMoreInteractions(repository);
		assertThat(readList.size(), is(entityList.size()));

		for (int i = 0; i < readList.size(); i++) {
			DTO dto = readList.get(i);
			ENT entity = entityList.get(i);
			checkFields(dto, entity);
		}
	}

	/**
	 * Check that findAll() method returns an empty DTO list if there are no
	 * entities.
	 */
	@Test
	public void findAllIsEmpty() {
		List<ENT> entityList = new ArrayList<ENT>();
		when(repository.findAll()).thenReturn(entityList);

		List<DTO> readList = service.findAll();

		verify(repository).findAll();
		verifyNoMoreInteractions(repository);
		assertThat(readList.size(), is(0));
	}

	/**
	 * Checks that returned DTO corresponds to read entity.
	 */
	@Test
	public void findByIdIsValid() {
		ENT entity = getNewEntity();
		when(repository.findOne(entity.getId())).thenReturn(Optional.of(entity));

		DTO dto = service.findById(entity.getId());

		verify(repository).findOne(1L);
		verifyNoMoreInteractions(repository);

		checkFields(dto, entity);
	}

	/**
	 * Checks that findById() throws {@link EntityNotFoundException} when entity
	 * is not found.
	 */
	@Test
	public void findByIdEntityNotFound() {
		when(repository.findOne(anyLong())).thenReturn(Optional.ofNullable(null));

		try {
			service.findById(1L);

			fail("Expected EntityNotFoundException");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(1L));
		}
	}

	/**
	 * Checks following aspects when called create() method:
	 * <ul>
	 * <li>The entity passed to repository matches the DTO passed to service.
	 * <li>The returned DTO from create() matches the entity returned by
	 * repository.
	 * <li>findBy...() methods are called in the repository to check for
	 * duplicated properties.
	 * <li>save() method is called in the repository.
	 * </ul>
	 */
	@Test
	public void createIsOk() {
		// Get entity from subclass and corresponding DTO from service.
		ENT entity = getNewEntity();
		DTO dto = service.getDtoFromEntity(entity);

		when(repository.save(any(entityType))).thenReturn(entity);

		DTO readDto = service.create(dto);

		// Verify call to repository.create().
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(entityType);
		verify(repository).save(entityCaptor.capture());
		verifyNoMoreInteractions(repository);

		// Check that entity passed to repository matches DTO passed to service.
		ENT capturedEntity = entityCaptor.getValue();
		checkFields(dto, capturedEntity);

		// Check that read DTO matches entity returned by repository.
		checkFields(readDto, entity);
	}

	/**
	 * Checks following aspects of service.update() method:
	 * <ul>
	 * <li>The entity passed to repository matches the DTO passed to service.
	 * <li>The returned DTO from update() matches the entity returned by
	 * repository.
	 * <li>findBy...() methods are called in the repository to check for
	 * duplicated properties.
	 * <li>finById() method is called in order to retrieve the most recent
	 * entity.
	 * <li>save() method is called in the repository.
	 * </ul>
	 */
	@Test
	public void updateIsOk() {
		// Get entity from subclass and corresponding DTO from service.
		ENT entity = getNewEntity();
		DTO dto = service.getDtoFromEntity(entity);

		when(repository.findOne(dto.getId())).thenReturn(Optional.of(entity));
		when(repository.save(any(entityType))).thenReturn(entity);

		DTO readDto = service.update(dto);

		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(entityType);
		verify(repository).findOne(dto.getId());
		verify(repository).save(entityCaptor.capture());
		verifyNoMoreInteractions(repository);

		// Check that entity passed to repository matches DTO passed to service.
		ENT capturedEntity = entityCaptor.getValue();
		checkFields(dto, capturedEntity);

		// Check that read DTO matches entity returned by repository.
		checkFields(readDto, entity);
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown if the entity to
	 * update does not exist.
	 */
	@Test
	public void updateEntityNotFound() {
		ENT entity = getNewEntity();
		DTO dto = service.getDtoFromEntity(entity);
		when(repository.findOne(dto.getId())).thenReturn(Optional.ofNullable(null));

		try {
			service.update(dto);
			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(dto.getId()));
		}

		verify(repository).findOne(dto.getId());
		verifyNoMoreInteractions(repository);
	}

	/**
	 * Tests the delete() method.
	 * <ul>
	 * <li>Checks that findOne() method is called to retrieve the entity to
	 * delete.</li>
	 * <li>Checks that delete method is called in the repository.</li>
	 * </ul>
	 */
	@Test
	public void deleteIsValid() {
		ENT entity = getNewEntity();

		when(repository.findOne(entity.getId())).thenReturn(Optional.of(entity));

		service.delete(entity.getId());

		verify(repository).findOne(entity.getId());
		verify(repository).delete(any(getEntityType()));
		verifyNoMoreInteractions(repository);
	}

	/**
	 * Tests the delete method when the entity to delete does not exist.
	 * <ul>
	 * <li>Checks that findOne() method is called to retrieve the entity to
	 * delete.</li>
	 * <li>Checks that EntityNotFoundException is thrown.</li>
	 * </ul> 
	 */
	@Test
	public void deleteEntityNotFound() {
		Long id = 1L;
		
		when(repository.findOne(id)).thenReturn(Optional.ofNullable(null));

		try {
			service.delete(id);
			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}

		verify(repository).findOne(id);
		verifyNoMoreInteractions(repository);
	}

	/**
	 * @return the service
	 */
	protected CrudServiceImpl<DTO, ENT> getService() {
		return service;
	}

}
