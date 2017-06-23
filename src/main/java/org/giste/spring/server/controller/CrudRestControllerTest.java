package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.giste.spring.server.service.CrudService;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.junit.Test;
import org.springframework.http.MediaType;

public abstract class CrudRestControllerTest<DTO extends BaseDto> extends BaseRestControllerTest<DTO> {

	@Override
	protected abstract CrudService<DTO> getService();

	/**
	 * Tests delete() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that findById() is called in service with the identifier of
	 * the DTO.</li>
	 * <li>Checks that delete() is called in service.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void deleteIsValid() throws Exception {
		final DTO dto = getNewDto();

		getMockMvc().perform(delete(getPathId(), dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());

		verify(getService()).delete(dto.getId());
		verifyNoMoreInteractions(getService());
	}

	@Test
	public void deleteEntityNotFound() throws Exception {
		final Long id = 1L;

		doThrow(new EntityNotFoundException(id, "Code", "Message", "Developer info")).when(getService()).delete(id);

		getMockMvc().perform(delete(getPathId(), id)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.code", is("Code")));

		verify(getService()).delete(id);
		verifyNoMoreInteractions(getService());
	}

}
