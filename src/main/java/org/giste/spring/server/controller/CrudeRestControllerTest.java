package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.giste.spring.server.service.CrudeService;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.NonRemovableDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public abstract class CrudeRestControllerTest<DTO extends NonRemovableDto> extends BaseRestControllerTest<DTO> {

	private static final String PATH_ENABLE = "/enable";
	private static final String PATH_DISABLE = "/disable";

	private String pathIdEnable;
	private String pathIdDisable;

	@Before
	public void setup() {
		super.setup();
		pathIdEnable = getPathId() + PATH_ENABLE;
		pathIdDisable = getPathId() + PATH_DISABLE;
	}

	@Override
	protected abstract CrudeService<DTO> getService();

	/**
	 * Checks that the response from the controller has all the needed
	 * properties with the correct values. Base implementation checks for
	 * <code>id</code> and <code>enabled</code> properties. Subclasses should
	 * call <code>super.checkExpectedProperties</code> and then check for the
	 * rest of DTO properties.
	 * 
	 * @param result ResultActions form the controller response.
	 * @param target Target DTO for checking properties.
	 * @return The response of the controller.
	 * @throws Exception If there is an error accessing ResultActions.
	 */
	protected ResultActions checkProperties(ResultActions result, DTO target) throws Exception {
		return super.checkProperties(result, target)
				.andExpect(jsonPath("$.enabled", is(target.isEnabled())));
	}

	/**
	 * Check that the response from controller has the DTO included with the
	 * correct properties. In this case, the response is a list of DTOs.
	 * 
	 * @param result The response form the controller.
	 * @param target The DTO whose properties should be in the response.
	 * @param index The index of the item to check in the list.
	 * @return The ResultActions.
	 * @throws Exception If there is an error accessing ResultActions.
	 */
	protected ResultActions checkListProperties(ResultActions result, DTO target, int index) throws Exception {
		return super.checkListProperties(result, target, index)
				.andExpect(jsonPath("$[" + index + "].enabled", is(target.isEnabled())));
	}

	/**
	 * Checks that a DTO as the same properties than a target DTO. Base
	 * implementation checks for <code>id</code> and <code>enabled</code>
	 * properties. Subclasses should call
	 * <code>super.checkExpectedProperties</code> and then check for the rest of
	 * DTO properties.
	 * 
	 * @param dto DTO to check.
	 * @param target Target DTO for checking.
	 */
	protected void checkDto(DTO dto, DTO target) {
		super.checkDto(dto, target);
		assertThat(dto.isEnabled(), is(target.isEnabled()));
	}

	/**
	 * Tests the enable() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is OK.</li>
	 * <li>Checks that response has the DTO with correct properties.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void enableIsValid() throws Exception {
		final DTO dto = getNewDto();

		when(getService().enable(dto.getId())).thenReturn(dto);

		ResultActions result = getMockMvc().perform(put(pathIdEnable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		verify(getService()).enable(dto.getId());
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests the enable() method when the entity is not found.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is NOT_FOUND.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void enableClubNotFound() throws Exception {
		final DTO dto = getNewDto();

		when(getService().enable(dto.getId()))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		getMockMvc().perform(put(pathIdEnable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(getService()).enable(dto.getId());
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests the disable() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is OK.</li>
	 * <li>Checks that response has the DTO with correct properties.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void disableIsValid() throws Exception {
		final DTO dto = getNewDto();

		when(getService().disable(dto.getId())).thenReturn(dto);

		ResultActions result = getMockMvc().perform(put(pathIdDisable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		verify(getService()).disable(dto.getId());
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests the disable() method when the entity is not found.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is NOT_FOUND.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void disableClubNotFound() throws Exception {
		final DTO dto = getNewDto();

		when(getService().disable(dto.getId()))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		getMockMvc().perform(put(pathIdDisable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(getService()).disable(dto.getId());
		verifyNoMoreInteractions(getService());
	}
}
