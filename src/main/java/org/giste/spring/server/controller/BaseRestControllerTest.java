package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.giste.spring.server.service.BaseService;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.BaseDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseRestControllerTest<DTO extends BaseDto> {

	private static final String PATH_ID = "/{id}";

	private String pathBase;
	private String pathId;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Gets the base path for this controller. It has the form
	 * <code>"/entities"</code>.
	 * 
	 * @return The base path.
	 */
	protected String getPathBase() {
		return pathBase;
	}

	/**
	 * Gets the path for a single entity request. It has the form
	 * <code>"/entities/{id}"</code>.
	 * 
	 * @return The path for a single entity request.
	 */
	protected String getPathId() {
		return pathId;
	}

	@Before
	public void setup() {
		pathBase = getPath();
		pathId = pathBase + PATH_ID;
	}

	/**
	 * Gets the service mock for testing.
	 * 
	 * @return The service mock.
	 */
	protected abstract BaseService<DTO> getService();

	/**
	 * Gets the MVC mock for testing.
	 * 
	 * @return The MVC mock.
	 */
	protected abstract MockMvc getMockMvc();

	/**
	 * Gets a new DTO to be used on tests.
	 * 
	 * @return The new DTO.
	 */
	protected abstract DTO getNewDto();

	/**
	 * Gets an invalid DTO from a valid one.
	 * 
	 * @param dto The valid DTO.
	 * @return The invalid DTO.
	 */
	protected abstract DTO getInvalidDto(DTO dto);

	/**
	 * Gets the base path for calls to the REST controller.
	 * 
	 * @return The base path.
	 */
	protected abstract String getPath();

	/**
	 * Gets the type of the DTO of the entity managed by the controller under
	 * testing.
	 * 
	 * @return The type of the DTO.
	 */
	protected abstract Class<DTO> getDtoType();

	/**
	 * Checks that the response from the controller has the correct invalid
	 * properties when an invalid DTO is used as parameter.
	 * 
	 * @param result The response from controller.
	 * @throws Exception if there is an error accessing the ResultActions.
	 */
	protected abstract void checkInvalidProperties(ResultActions result) throws Exception;

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
		return result.andExpect(jsonPath("$.id", is(target.getId().intValue())));
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
		return result.andExpect(jsonPath("$[" + index + "].id", is(target.getId().intValue())));
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
		assertThat(dto.getId(), is(target.getId()));
	}

	/**
	 * Tests the create() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Calls the POST method in controller.</li>
	 * <li>Checks that the response is OK.</li>
	 * <li>Checks the properties in the response.</li>
	 * <li>Verify the right calls to service.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void createIsValid() throws Exception {
		final DTO dto = getNewDto();
		when(getService().create(any(getDtoType()))).thenReturn(dto);

		ResultActions result = getMockMvc().perform(post(pathBase)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(getDtoType());
		verify(getService()).create(dtoCaptor.capture());
		verifyNoMoreInteractions(getService());

		DTO capturedDto = dtoCaptor.getValue();
		checkDto(capturedDto, dto);
	}

	/**
	 * Test the create() method when a DTO with invalid properties is used as
	 * parameter.
	 * <ul>
	 * <li>Gets an invalid DTO from superclass.</li>
	 * <li>Calls the controller.</li>
	 * <li>Checks the response is BAD_REQUEST.</li>
	 * <li>Call superclass to check the invalid properties.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void createInvalidDto() throws Exception {
		final DTO dto = getInvalidDto(getNewDto());

		ResultActions result = getMockMvc().perform(post(pathBase)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkInvalidProperties(result);

		verifyZeroInteractions(getService());
	}

	/**
	 * Test the findById() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Perform the call to controller.</li>
	 * <li>Check that status is OK.</li>
	 * <li>Check the properties in the response.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void findByIdIsValid() throws Exception {
		final DTO dto = getNewDto();
		when(getService().findById(dto.getId())).thenReturn(dto);

		ResultActions result = getMockMvc().perform(get(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		verify(getService()).findById(dto.getId());
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests findById() method when the identifier is not found.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Check that the response is NOT_FOUND.</li>
	 * <li>Check that response has the error properties.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void findByIdEntityNotFound() throws Exception {
		final Long id = 100L;
		final EntityNotFoundException enfe = new EntityNotFoundException(id, "Code", "Message", "Developer Info");

		when(getService().findById(id)).thenThrow(enfe);

		getMockMvc().perform(get(pathId, id)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.code", is(enfe.getCode())))
				.andExpect(jsonPath("$.message", is(enfe.getMessage())))
				.andExpect(jsonPath("$.developerInfo", is(enfe.getDeveloperInfo())));

		verify(getService()).findById(id);
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests findAll() method.
	 * <ul>
	 * <li>Construct a list of DTOs.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is OK.</li>
	 * <li>Checks that all the items in the response have the correct
	 * properties.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void findAllIsValid() throws Exception {
		final DTO dto1 = getNewDto();
		final DTO dto2 = getNewDto();
		final List<DTO> dtoList = new ArrayList<DTO>();
		dtoList.add(dto1);
		dtoList.add(dto2);

		when(getService().findAll()).thenReturn(dtoList);

		ResultActions result = getMockMvc().perform(get(pathBase)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$", hasSize(dtoList.size())));

		for (int i = 0; i < dtoList.size(); i++) {
			checkListProperties(result, dtoList.get(i), i);
		}

		verify(getService()).findAll();
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests the findAll() method with an empty list.
	 * <ul>
	 * <li>Construct an empty list of DTOs.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is OK.</li>
	 * <li>Checks that there are no items in the response.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void findAllIsEmpty() throws Exception {
		final List<DTO> dtoList = new ArrayList<DTO>();

		when(getService().findAll()).thenReturn(dtoList);

		getMockMvc().perform(get(pathBase)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$", hasSize(0)));

		verify(getService()).findAll();
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Tests the update() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is OK.</li>
	 * <li>Checks that DTO passed to mocked service is the right one.</li>
	 * <li>Checks that response matches the DTO.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void updateIsValid() throws Exception {
		final DTO dto = getNewDto();

		when(getService().update(any(getDtoType()))).thenReturn(dto);

		ResultActions result = getMockMvc().perform(put(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(getDtoType());
		verify(getService()).update(dtoCaptor.capture());
		verifyNoMoreInteractions(getService());

		DTO capturedDto = dtoCaptor.getValue();
		checkDto(capturedDto, dto);
	}

	/**
	 * Test the update() method when a DTO with invalid properties is used as
	 * parameter.
	 * <ul>
	 * <li>Gets an invalid DTO from superclass.</li>
	 * <li>Calls the controller.</li>
	 * <li>Checks the response is BAD_REQUEST.</li>
	 * <li>Call superclass to check the invalid properties.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void updateInvalidDto() throws Exception {
		final DTO dto = getInvalidDto(getNewDto());

		ResultActions result = this.getMockMvc().perform(put(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkInvalidProperties(result);

		verifyZeroInteractions(getService());
	}

	/**
	 * Tests update() method when the DTO is not found.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is NOT_FOUND</li>
	 * <li>Checks that the returned error is the right one.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void updateEntityNotFound() throws Exception {
		final DTO dto = getNewDto();

		when(getService().update(any(getDtoType())))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		getMockMvc().perform(put(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(getService()).update(any(getDtoType()));
		verifyNoMoreInteractions(getService());
	}

	/**
	 * Checks the update() method when the id of the DTO is different that the
	 * one in the URI.
	 * <ul>
	 * <li>Gets a new DTO from superclass and modify its identifier.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that response is OK.</li>
	 * <li>Checks that the updated DTO is the one indicated by URI.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void updateDifferentIdThanUri() throws Exception {
		final DTO dto = getNewDto();
		dto.setId(2L);
		final DTO updatedDto = getNewDto();
		updatedDto.setId(1L);

		when(getService().update(any(getDtoType()))).thenReturn(updatedDto);

		getMockMvc().perform(put(pathId, updatedDto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(updatedDto.getId().intValue())));

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(getDtoType());
		verify(getService()).update(dtoCaptor.capture());
		verifyNoMoreInteractions(getService());

		DTO capturedClub = dtoCaptor.getValue();
		assertThat(capturedClub.getId(), is(updatedDto.getId()));
	}

}
