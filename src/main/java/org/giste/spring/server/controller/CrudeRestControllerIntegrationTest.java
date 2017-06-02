package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.giste.spring.util.error.dto.RestErrorDto;
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
public abstract class CrudeRestControllerIntegrationTest<DTO extends NonRemovableDto> {

	protected List<DTO> dtoList;
	protected Class<DTO> dtoType;
	protected Class<DTO[]> arrayType;
	protected String pathBase;
	protected String pathId;
	protected String pathEnable;
	protected String pathDisable;

	@Autowired
	protected TestRestTemplate restTemplate;

	@Before
	public void setUp() {
		this.dtoList = getDtoList();

		this.pathBase = getPath();
		this.pathId = pathBase + "/{id}";
		this.pathEnable = pathId + "/enable";
		this.pathDisable = pathId + "/disable";

		this.dtoType = getDtoType();
		this.arrayType = getArrayType();
	}

	@Test
	public void findAll() {
		// Get all clubs.
		List<DTO> readDtoList = Arrays.stream(restTemplate.getForObject(getPathBase(), arrayType))
				.collect(Collectors.toList());
		// Check list size.
		assertThat(readDtoList.size(), is(dtoList.size()));
		// Check clubs.
		checkDto(readDtoList.get(0), dtoList.get(0), true);
		checkDto(readDtoList.get(1), dtoList.get(1), true);

		if (readDtoList.size() != this.dtoList.size()) {
			fail("The number of found entities has to be " + this.dtoList.size());
		}

		for (int i = 0; i < readDtoList.size(); i++) {
			checkDto(readDtoList.get(i), this.dtoList.get(i), true);
		}

	}

	@Test
	public void findByIdIsValid() {
		DTO readDto = this.restTemplate.getForObject(getPathId(),
				dtoType, dtoList.get(0).getId());
		checkDto(readDto, dtoList.get(0), true);
	}

	@Test
	public void findByIdEntityNotFound() {
		RestErrorDto restError = this.restTemplate.getForObject(getPathId(),
				RestErrorDto.class, 100L);

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Test
	public void createIsValid() {
		DTO dto = getNewDto();

		DTO readDto = this.restTemplate.postForObject(getPathBase(), dto,
				dtoType);

		checkDto(readDto, dto, false);
	}

	@Test
	public void createIsInvalid() {
		DTO dto = getInvalidDto(getNewDto());

		RestErrorDto restError = this.restTemplate.postForObject(getPathBase(), dto,
				RestErrorDto.class);

		assertThat(restError.getFieldErrorList().size(), is(getInvalidProperties()));
	}

	@Test
	public void updateIsValid() {
		DTO dto = getUpdatedDto(dtoList.get(0));

		DTO readDto = this.restTemplate.exchange(getPathId(), HttpMethod.PUT, new HttpEntity<>(dto),
				dtoType, dto.getId()).getBody();

		checkDto(readDto, dto, true);
	}

	@Test
	public void updateIsInvalid() {
		DTO dto = getInvalidDto(dtoList.get(0));

		RestErrorDto restError = this.restTemplate.exchange(getPathId(), HttpMethod.PUT, new HttpEntity<>(dto),
				RestErrorDto.class, dto.getId()).getBody();

		assertThat(restError.getFieldErrorList().size(), is(getInvalidProperties()));
	}

	@Test
	public void updateNotFound() {
		DTO dto = getUpdatedDto(dtoList.get(0));
		dto.setId(100L);

		RestErrorDto restError = this.restTemplate.exchange(getPathId(), HttpMethod.PUT, new HttpEntity<>(dto),
				RestErrorDto.class, dto.getId()).getBody();

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Test
	public void enableIsValid() {
		DTO dto = getDisabledDto();

		DTO readDto = this.restTemplate.exchange(getPathEnable(), HttpMethod.PUT, new HttpEntity<>(dto),
				dtoType, dto.getId()).getBody();

		dto.setEnabled(true);

		checkDto(readDto, dto, true);
	}

	@Test
	public void enableNotFound() {
		DTO dto = getDisabledDto();
		dto.setId(100L);

		RestErrorDto restError = this.restTemplate.exchange(getPathEnable(), HttpMethod.PUT, new HttpEntity<>(dto),
				RestErrorDto.class, dto.getId()).getBody();

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Test
	public void disableIsValid() {
		DTO dto = getEnabledDto();

		DTO readDto = this.restTemplate.exchange(getPathDisable(), HttpMethod.PUT, new HttpEntity<>(dto),
				dtoType, dto.getId()).getBody();

		dto.setEnabled(false);

		checkDto(readDto, dto, true);
	}

	@Test
	public void disableNotFound() {
		DTO dto = getEnabledDto();
		dto.setId(100L);

		RestErrorDto restError = this.restTemplate.exchange(getPathDisable(), HttpMethod.PUT, new HttpEntity<>(dto),
				RestErrorDto.class, dto.getId()).getBody();

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	protected String getPathBase() {
		return pathBase;
	}

	protected String getPathId() {
		return pathId;
	}

	protected String getPathEnable() {
		return pathEnable;
	}

	protected String getPathDisable() {
		return pathDisable;
	}

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
	 * Gets the path of the entity under testing.
	 * 
	 * @return The path of the entity under testing.
	 */
	protected abstract String getPath();

	/**
	 * Gets the error code for this entity returned when looked up by its
	 * identifier and it's not found.
	 * 
	 * @return The error code returned in <code>RestErrorDto</code>.
	 */
	protected abstract String getNotFoundErrorCode();

	/**
	 * Gets a DTO that is disabled.
	 * 
	 * @return The DTO.
	 */
	protected abstract DTO getDisabledDto();

	/**
	 * Gets a DTO that is enabled.
	 * 
	 * @return The DTO.
	 */
	protected abstract DTO getEnabledDto();
}