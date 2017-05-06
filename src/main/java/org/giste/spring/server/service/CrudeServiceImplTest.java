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

import org.giste.spring.server.entity.NonRemovableEntity;
import org.giste.spring.server.repository.CrudeRepository;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.NonRemovableDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.GenericTypeResolver;

/**
 * Base abstract class for testing CRUDE services. It performs common tests for
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
public abstract class CrudeServiceImplTest<DTO extends NonRemovableDto, ENT extends NonRemovableEntity> {

	private CrudeRepository<ENT> repository;

	protected CrudeServiceImpl<DTO, ENT> service;

	private Class<ENT> entityType;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		repository = getRepositoryMock();
		service = getService();

		this.entityType = (Class<ENT>) GenericTypeResolver.resolveTypeArgument(getClass(), NonRemovableEntity.class);
	}

	/**
	 * Gets the mocked repository to be used by service under testing. This
	 * method is called during setup.
	 * 
	 * @return The repository.
	 */
	protected abstract CrudeRepository<ENT> getRepositoryMock();

	/**
	 * Gets the service under testing. This method is called during setup.
	 * 
	 * @return The service under testing.
	 */
	protected abstract CrudeServiceImpl<DTO, ENT> getService();

	/**
	 * Gets an entity used for testing. Tests in this class can modify the
	 * identifier and the enabled state of the entity.
	 * 
	 * @return The entity to use for testing.
	 */
	protected abstract ENT getNewEntity();

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
		assertThat(dto.isEnabled(), is(entity.isEnabled()));
	}

	/**
	 * Allow subclasses to verify that findBy...() methods are called in
	 * repository when the service checks for duplicated properties before
	 * creating or updating one entity.
	 * 
	 * @param dto The DTO to check.
	 */
	protected void verifyDuplicatedProperties(DTO dto) {

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
		when(repository.findOne(1L)).thenReturn(entity);

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
		when(repository.findOne(anyLong())).thenReturn(null);

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

		// Allow subclass to verify calls to repository.findBy...() methods.
		verifyDuplicatedProperties(dto);

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

		when(repository.findOne(dto.getId())).thenReturn(entity);
		when(repository.save(any(entityType))).thenReturn(entity);

		DTO readDto = service.update(dto);

		// Allow subclass to verify calls to repository.findBy...() methods.
		verifyDuplicatedProperties(dto);

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
		when(repository.findOne(dto.getId())).thenReturn(null);

		try {
			service.update(dto);

			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(dto.getId()));
		}

		verifyDuplicatedProperties(dto);
		verify(repository).findOne(dto.getId());
		verifyNoMoreInteractions(repository);
	}

	/**
	 * Checks the call to enable() method.
	 * <ul>
	 * <li>findById() is called in order to retrieve the entity.
	 * <li>save() method is called and the entity has correct id and is enabled.
	 * <li>Returned DTO has correct id and is enabled.
	 * </ul>
	 */
	@Test
	public void enableIsOk() throws Exception {
		ENT disabledEntity = getNewEntity();
		disabledEntity.setEnabled(false);
		ENT enabledEntity = getNewEntity();
		enabledEntity.setId(disabledEntity.getId());
		enabledEntity.setEnabled(true);

		when(repository.findOne(disabledEntity.getId())).thenReturn(disabledEntity);
		when(repository.save(any(entityType))).thenReturn(enabledEntity);

		DTO readDto = service.enable(disabledEntity.getId());

		verify(repository).findOne(disabledEntity.getId());
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(entityType);
		verify(repository).save(entityCaptor.capture());

		// Check that entity passed to repository matches the id and is enabled.
		ENT capturedClub = entityCaptor.getValue();
		assertThat(capturedClub.getId(), is(disabledEntity.getId()));
		assertThat(capturedClub.isEnabled(), is(true));

		// Check that read DTO has the correct id and is enabled.
		assertThat(readDto.getId(), is(enabledEntity.getId()));
		assertThat(readDto.isEnabled(), is(true));
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown when the entity to
	 * enable does not exist.
	 */
	@Test
	public void enableEntityNotFound() {
		final Long id = 1L;
		when(repository.findOne(id)).thenReturn(null);

		try {
			service.enable(id);

			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}

		verify(repository).findOne(id);
		verifyNoMoreInteractions(repository);
	}

	/**
	 * Checks the call to disable() method.
	 * <ul>
	 * <li>findById() is called in order to retrieve the entity.
	 * <li>save() method is called and the entity has correct id and is
	 * disabled.
	 * <li>Returned DTO has correct id and is disabled.
	 * </ul>
	 */
	@Test
	public void disableIsOk() {
		ENT enabledEntity = getNewEntity();
		enabledEntity.setEnabled(true);
		ENT disabledEntity = getNewEntity();
		disabledEntity.setId(enabledEntity.getId());
		disabledEntity.setEnabled(false);

		when(repository.findOne(enabledEntity.getId())).thenReturn(enabledEntity);
		when(repository.save(any(entityType))).thenReturn(disabledEntity);

		DTO readDto = service.disable(enabledEntity.getId());

		verify(repository).findOne(enabledEntity.getId());
		ArgumentCaptor<ENT> entityCaptor = ArgumentCaptor.forClass(entityType);
		verify(repository).save(entityCaptor.capture());

		// Check that entity passed to repository matches the id and is
		// disabled.
		ENT capturedClub = entityCaptor.getValue();
		assertThat(capturedClub.getId(), is(enabledEntity.getId()));
		assertThat(capturedClub.isEnabled(), is(false));

		// Check that read DTO has the correct id and is disabled.
		assertThat(readDto.getId(), is(disabledEntity.getId()));
		assertThat(readDto.isEnabled(), is(false));
	}

	/**
	 * Checks that {@link EntityNotFoundException} is thrown when the entity to
	 * disable does not exist.
	 */
	@Test
	public void disableEntityNotFound() throws Exception {
		final Long id = 1L;
		when(repository.findOne(id)).thenReturn(null);

		try {
			service.disable(id);

			fail("EntityNotFoundException expected.");
		} catch (EntityNotFoundException e) {
			assertThat(e.getId(), is(id));
		}

		verify(repository).findOne(id);
		verifyNoMoreInteractions(repository);
	}
}
