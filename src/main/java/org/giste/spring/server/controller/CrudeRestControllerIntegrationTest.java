package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.giste.spring.util.error.dto.RestErrorDto;
import org.giste.util.dto.NonRemovableDto;
import org.junit.Before;
import org.junit.Test;
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
public abstract class CrudeRestControllerIntegrationTest<DTO extends NonRemovableDto>
		extends BaseRestControllerIntegrationTest<DTO> {

	private String pathEnable;
	private String pathDisable;

	protected String getPathEnable() {
		return pathEnable;
	}

	protected String getPathDisable() {
		return pathDisable;
	}

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

	@Before
	public void setUp() {
		super.setUp();
		this.pathEnable = getPathId() + "/enable";
		this.pathDisable = getPathId() + "/disable";
	}

	@Test
	public void enableIsValid() {
		DTO dto = getDisabledDto();

		DTO readDto = getRestTemplate()
				.exchange(getPathEnable(), HttpMethod.PUT, new HttpEntity<>(dto), getDtoType(), dto.getId()).getBody();

		dto.setEnabled(true);

		checkDto(readDto, dto, true);
	}

	@Test
	public void enableNotFound() {
		DTO dto = getDisabledDto();
		dto.setId(100L);

		RestErrorDto restError = getRestTemplate()
				.exchange(getPathEnable(), HttpMethod.PUT, new HttpEntity<>(dto), RestErrorDto.class, dto.getId())
				.getBody();

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Test
	public void disableIsValid() {
		DTO dto = getEnabledDto();

		DTO readDto = getRestTemplate()
				.exchange(getPathDisable(), HttpMethod.PUT, new HttpEntity<>(dto), getDtoType(), dto.getId()).getBody();

		dto.setEnabled(false);

		checkDto(readDto, dto, true);
	}

	@Test
	public void disableNotFound() {
		DTO dto = getEnabledDto();
		dto.setId(100L);

		RestErrorDto restError = getRestTemplate()
				.exchange(getPathDisable(), HttpMethod.PUT, new HttpEntity<>(dto), RestErrorDto.class, dto.getId())
				.getBody();

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Override
	public void findAll() {
		// Get all items.
		List<DTO> readDtoList = Arrays.stream(getRestTemplate().getForObject(getPathBase(), getArrayType()))
				.collect(Collectors.toList());

		// Get enabled items.
		List<DTO> enabledDtoList = getDtoList().stream().filter(t -> t.isEnabled()).collect(Collectors.toList());

		// Check list size.
		assertThat(readDtoList.size(), is(enabledDtoList.size()));

		for (int i = 0; i < readDtoList.size(); i++) {
			checkDto(readDtoList.get(i), enabledDtoList.get(i), true);
		}
	}

}