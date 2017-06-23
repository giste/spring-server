package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.giste.spring.util.error.dto.RestErrorDto;
import org.giste.util.dto.BaseDto;
import org.giste.util.dto.NonRemovableDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Base class for integration testing of CRUDE Rest controllers. It has common
 * tests for this kind of controllers.
 * 
 * @author Giste
 *
 * @param <DTO> DTO of the entity managed by the controller.
 */
public abstract class BaseRestControllerIntegrationTest<DTO extends BaseDto> {

	private String pathBase;
	private String pathId;

	@Autowired
	private TestRestTemplate restTemplate;

	/**
	 * Gets the base path for the controller.
	 * 
	 * @return The base path for the controller.
	 */
	protected String getPathBase() {
		return pathBase;
	}

	/**
	 * Gets the path for actions over single entities.
	 * 
	 * @return The path for actions over single entities.
	 */
	protected String getPathId() {
		return pathId;
	}

	/**
	 * Gets the RestTemplate used for testing.
	 * 
	 * @return The RestTemplate used for testing.
	 */
	protected TestRestTemplate getRestTemplate() {
		return restTemplate;
	}

	/**
	 * Checks that one DTO has the same values that the target DTO.
	 * 
	 * @param dto DTO to check.
	 * @param target DTO with target values.
	 * @param checkId If <code>true</code>, the identifier is checked. If
	 *            <code>false</code>, it's not checked.
	 */
	protected abstract void checkDto(DTO dto, DTO target, boolean checkId);

	/**
	 * Gets the list of DTOs for testing. At least, needs to have one element.
	 * 
	 * @return The list of DTOs for testing.
	 */
	protected abstract List<DTO> getDtoList();

	/**
	 * Gets a new DTO. It's used for <code>create()</code> testing.
	 * 
	 * @return A DTO without identifier.
	 */
	protected abstract DTO getNewDto();

	/**
	 * Constructs a DTO with invalid properties from a valid one.
	 * 
	 * @param dto DTO to change into an invalid one.
	 * @return DTO with invalid properties.
	 */
	protected abstract DTO getInvalidDto(DTO dto);

	/**
	 * Changes the values of one DTO for testing <code>update()</code> methods.
	 * 
	 * @param dto DTO to be updated.
	 * @return DTO with updated values.
	 */
	protected abstract DTO getUpdatedDto(DTO dto);

	/**
	 * Gets the number of invalid properties of the DTO returned in
	 * {@link #getInvalidDto(NonRemovableDto)}.
	 * 
	 * @return The number of invalid properties.
	 */
	protected abstract int getInvalidProperties();

	/**
	 * Gets the type of the DTO under testing.
	 * 
	 * @return The type of the DTO under testing.
	 */
	protected abstract Class<DTO> getDtoType();

	/**
	 * Gets the type of the DTO array under testing.
	 * 
	 * @return The type of the DTO array under testing.
	 */
	protected abstract Class<DTO[]> getArrayType();

	/**
	 * Gets the path of the controller under testing.
	 * 
	 * @return The path of the controller under testing.
	 */
	protected abstract String getPath();

	/**
	 * Gets the error code for this entity returned when looked up by its
	 * identifier and it's not found.
	 * 
	 * @return The error code returned in <code>RestErrorDto</code>.
	 */
	protected abstract String getNotFoundErrorCode();

	@Before
	public void setUp() {
		this.pathBase = getPath();
		this.pathId = pathBase + "/{id}";
	}

	@Test
	public void findAll() {
		// Get all clubs.
		List<DTO> readDtoList = Arrays.stream(restTemplate.getForObject(getPathBase(), getArrayType()))
				.collect(Collectors.toList());
		// Check list size.
		assertThat(readDtoList.size(), is(getDtoList().size()));
		// Check clubs.
		checkDto(readDtoList.get(0), getDtoList().get(0), true);
		checkDto(readDtoList.get(1), getDtoList().get(1), true);

		if (readDtoList.size() != getDtoList().size()) {
			fail("The number of found entities has to be " + getDtoList().size());
		}

		for (int i = 0; i < readDtoList.size(); i++) {
			checkDto(readDtoList.get(i), getDtoList().get(i), true);
		}

	}

	@Test
	public void findByIdIsValid() {
		DTO readDto = restTemplate.getForObject(getPathId(),
				getDtoType(), getDtoList().get(0).getId());
		checkDto(readDto, getDtoList().get(0), true);
	}

	@Test
	public void findByIdEntityNotFound() {
		RestErrorDto restError = restTemplate.getForObject(getPathId(),
				RestErrorDto.class, 100L);

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Test
	public void createIsValid() {
		DTO dto = getNewDto();

		DTO readDto = restTemplate.postForObject(getPathBase(), dto,
				getDtoType());

		checkDto(readDto, dto, false);
	}

	@Test
	public void createIsInvalid() {
		DTO dto = getInvalidDto(getNewDto());

		RestErrorDto restError = restTemplate.postForObject(getPathBase(), dto,
				RestErrorDto.class);

		assertThat(restError.getFieldErrorList().size(), is(getInvalidProperties()));
	}

	@Test
	public void updateIsValid() {
		DTO dto = getUpdatedDto(getDtoList().get(0));

		DTO readDto = restTemplate.exchange(getPathId(), HttpMethod.PUT, new HttpEntity<>(dto),
				getDtoType(), dto.getId()).getBody();

		checkDto(readDto, dto, true);
	}

	@Test
	public void updateIsInvalid() {
		DTO dto = getInvalidDto(getDtoList().get(0));

		RestErrorDto restError = restTemplate.exchange(getPathId(), HttpMethod.PUT, new HttpEntity<>(dto),
				RestErrorDto.class, dto.getId()).getBody();

		assertThat(restError.getFieldErrorList().size(), is(getInvalidProperties()));
	}

	@Test
	public void updateNotFound() {
		DTO dto = getUpdatedDto(getDtoList().get(0));
		dto.setId(100L);

		RestErrorDto restError = restTemplate.exchange(getPathId(), HttpMethod.PUT, new HttpEntity<>(dto),
				RestErrorDto.class, dto.getId()).getBody();

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

}