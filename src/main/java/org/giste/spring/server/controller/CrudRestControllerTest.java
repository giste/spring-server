package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.giste.spring.server.service.CrudService;
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

public abstract class CrudRestControllerTest<DTO extends BaseDto> {
	// URIs.
	private String basePath;
	private String pathId;

	private static final String PATH_ID = "/{id}";

	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	private CrudService<DTO> service;

	private Class<DTO> dtoType;

	@Before
	public void setup() {
		service = getService();
		mvc = getMockMvc();
		basePath = getBasePath();
		pathId = basePath + PATH_ID;

		this.dtoType = getDtoType();
	}

	/**
	 * Gets the service mock for testing.
	 * 
	 * @return The service mock.
	 */
	protected abstract CrudService<DTO> getService();

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
	protected abstract String getBasePath();

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
		when(service.create(any(dtoType))).thenReturn(dto);

		ResultActions result = this.mvc.perform(post(basePath)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(dtoType);
		verify(service).create(dtoCaptor.capture());
		verifyNoMoreInteractions(service);

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

		ResultActions result = this.mvc.perform(post(basePath)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkInvalidProperties(result);

		verifyZeroInteractions(service);
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
		when(service.findById(dto.getId())).thenReturn(dto);

		ResultActions result = this.mvc.perform(get(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		verify(service).findById(dto.getId());
		verifyNoMoreInteractions(service);
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

		when(service.findById(id)).thenThrow(enfe);

		mvc.perform(get(pathId, id)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.code", is(enfe.getCode())))
				.andExpect(jsonPath("$.message", is(enfe.getMessage())))
				.andExpect(jsonPath("$.developerInfo", is(enfe.getDeveloperInfo())));

		verify(service).findById(id);
		verifyNoMoreInteractions(service);
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

		when(service.findAll()).thenReturn(dtoList);

		ResultActions result = mvc.perform(get(basePath)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$", hasSize(dtoList.size())));

		for (int i = 0; i < dtoList.size(); i++) {
			checkListProperties(result, dtoList.get(i), i);
		}

		verify(service).findAll();
		verifyNoMoreInteractions(service);
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

		when(service.findAll()).thenReturn(dtoList);

		mvc.perform(get(basePath)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$", hasSize(0)));

		verify(service).findAll();
		verifyNoMoreInteractions(service);
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

		when(service.update(any(dtoType))).thenReturn(dto);

		ResultActions result = mvc.perform(put(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkProperties(result, dto);

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(dtoType);
		verify(service).update(dtoCaptor.capture());
		verifyNoMoreInteractions(service);

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

		ResultActions result = this.mvc.perform(put(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkInvalidProperties(result);

		verifyZeroInteractions(service);
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

		when(service.update(any(dtoType)))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		mvc.perform(put(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(service).update(any(dtoType));
		verifyNoMoreInteractions(service);
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

		when(service.update(any(dtoType))).thenReturn(updatedDto);

		mvc.perform(put(pathId, updatedDto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(updatedDto.getId().intValue())));

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(dtoType);
		verify(service).update(dtoCaptor.capture());
		verifyNoMoreInteractions(service);

		DTO capturedClub = dtoCaptor.getValue();
		assertThat(capturedClub.getId(), is(updatedDto.getId()));
	}

	/**
	 * Tests delete() method.
	 * <ul>
	 * <li>Gets a new DTO from superclass.</li>
	 * <li>Performs the call to controller.</li>
	 * <li>Checks that findById() is called in service with the identifier of the DTO.</li>
	 * <li>Checks that delete() is called in service.</li>
	 * </ul>
	 * 
	 * @throws Exception If there is an error calling the controller.
	 */
	@Test
	public void deleteIsValid() throws Exception {
		final DTO dto = getNewDto();

		//when(service.findById(dto.getId())).thenReturn(dto);

		mvc.perform(delete(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
		
		verify(service).delete(dto.getId());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void deleteEntityNotFound() throws Exception {
		final Long id = 1L;
		
		doThrow(new EntityNotFoundException(id, "Code", "Message", "Developer info")).when(service).delete(id);
		
		mvc.perform(delete(pathId, id)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.code", is("Code")));
		
		verify(service).delete(id);
		verifyNoMoreInteractions(service);
	}
}
