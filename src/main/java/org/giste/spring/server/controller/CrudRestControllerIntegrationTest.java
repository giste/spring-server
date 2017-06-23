package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.giste.spring.util.error.dto.RestErrorDto;
import org.giste.util.dto.BaseDto;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Base class for integration testing of CRUDE Rest controllers. It has common
 * tests for this kind of controllers.
 * 
 * @author Giste
 *
 * @param <DTO> DTO of the entity managed by the controller.
 */
public abstract class CrudRestControllerIntegrationTest<DTO extends BaseDto>
		extends BaseRestControllerIntegrationTest<DTO> {

	@Test
	public void deleteIsValid() {
		List<DTO> dtoList = getDtoList();
		Long id = dtoList.get(0).getId();

		getRestTemplate().delete(getPathId(), id);

		RestErrorDto restError = getRestTemplate().getForObject(getPathId(),
				RestErrorDto.class, id);

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

	@Test
	public void deleteEntityNotFound() {
		RestErrorDto restError = getRestTemplate().getForObject(getPathId(),
				RestErrorDto.class, 100L);

		assertThat(restError.getStatus(), is(HttpStatus.NOT_FOUND));
		assertThat(restError.getCode(), is(getNotFoundErrorCode()));
	}

}