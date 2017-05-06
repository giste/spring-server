package org.giste.spring.server.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.giste.spring.server.service.CrudeService;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.util.dto.NonRemovableDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class RestCrudeControllerTest<DTO extends NonRemovableDto> {
	// URIs.
	private String basePath;
	private String pathId;
	private String pathIdEnable;
	private String pathIdDisable;

	private static final String PATH_ID = "/{id}";
	private static final String PATH_ENABLE = "/enable";
	private static final String PATH_DISABLE = "/disable";

	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	private CrudeService<DTO> service;

	private Class<DTO> dtoType;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		service = getService();
		mvc = getMockMvc();
		basePath = getBasePath();
		pathId = basePath + PATH_ID;
		pathIdEnable = pathId + PATH_ENABLE;
		pathIdDisable = pathId + PATH_DISABLE;

		this.dtoType = (Class<DTO>) GenericTypeResolver.resolveTypeArgument(getClass(), NonRemovableDto.class);
	}

	protected abstract CrudeService<DTO> getService();
	
	protected abstract MockMvc getMockMvc();

	protected abstract DTO getNewDto();

	protected abstract String getBasePath();

	protected ResultActions checkExpectedProperties(ResultActions result, DTO dto) throws Exception {
		return result.andExpect(jsonPath("$.id", is(dto.getId().intValue())))
				.andExpect(jsonPath("$.enabled", is(dto.isEnabled())));
	}

	protected void checkProperties(DTO dto, DTO targetDto) {
		assertThat(dto.getId(), is(targetDto.getId()));
		assertThat(dto.isEnabled(), is(targetDto.isEnabled()));
	}

	@Test
	public void createIsValid() throws Exception {
		final DTO dto = getNewDto();
		when(service.create(any(dtoType))).thenReturn(dto);

		ResultActions result = this.mvc.perform(post(basePath)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkExpectedProperties(result, dto);

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(dtoType);
		verify(service).create(dtoCaptor.capture());
		verifyNoMoreInteractions(service);

		DTO capturedDto = dtoCaptor.getValue();
		checkProperties(capturedDto, dto);
	}

	@Test
	public void findByIdIsValid() throws Exception {
		final DTO dto = getNewDto();
		when(service.findById(dto.getId())).thenReturn(dto);

		ResultActions result = this.mvc.perform(get(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkExpectedProperties(result, dto);

		verify(service).findById(dto.getId());
		verifyNoMoreInteractions(service);
	}

	@Test
	public void findByIdClubNotFound() throws Exception {
		DTO dto = getNewDto();
		when(service.findById(dto.getId()))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		mvc.perform(get(pathId, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(service).findById(1L);
		verifyNoMoreInteractions(service);
	}

	@Test
	public void findAllIsValid() throws Exception {
		final DTO dto1 = getNewDto();
		final DTO dto2 = getNewDto();
		final List<DTO> dtoList = new ArrayList<DTO>();
		dtoList.add(dto1);
		dtoList.add(dto2);

		when(service.findAll()).thenReturn(dtoList);

		mvc.perform(get(basePath)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is(dto1.getId().intValue())))
				.andExpect(jsonPath("$[0].enabled", is(dto1.isEnabled())))
				.andExpect(jsonPath("$[1].id", is(dto2.getId().intValue())))
				.andExpect(jsonPath("$[1].enabled", is(dto2.isEnabled())));

		verify(service).findAll();
		verifyNoMoreInteractions(service);
	}

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

	@Test
	public void updateIsValid() throws Exception {
		final DTO dto = getNewDto();

		when(service.update(any(dtoType))).thenReturn(dto);

		ResultActions result = mvc.perform(put(pathId, 1)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkExpectedProperties(result, dto);

		ArgumentCaptor<DTO> dtoCaptor = ArgumentCaptor.forClass(dtoType);
		verify(service).update(dtoCaptor.capture());
		verifyNoMoreInteractions(service);

		DTO capturedDto = dtoCaptor.getValue();
		checkProperties(capturedDto, dto);
	}

	@Test
	public void updateClubNotFound() throws Exception {
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

	@Test
	public void updateDifferentIdThanUri() throws Exception {
		final DTO dto = getNewDto();
		dto.setId(2L);
		final DTO updatedDto = getNewDto();
		updatedDto.setId(1L);

		when(service.update(any(dtoType))).thenReturn(updatedDto);

		mvc.perform(put(pathId, 1L)
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

	@Test
	public void enableIsValid() throws Exception {
		final DTO dto = getNewDto();

		when(service.enable(dto.getId())).thenReturn(dto);

		ResultActions result = mvc.perform(put(pathIdEnable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		checkExpectedProperties(result, dto);

		verify(service).enable(dto.getId());
		verifyNoMoreInteractions(service);
	}

	@Test
	public void enableClubNotFound() throws Exception {
		final DTO dto = getNewDto();

		when(service.enable(dto.getId()))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		mvc.perform(put(pathIdEnable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(service).enable(dto.getId());
		verifyNoMoreInteractions(service);
	}

	@Test
	public void disableIsValid() throws Exception {
		final DTO dto = getNewDto();

		when(service.disable(dto.getId())).thenReturn(dto);

		ResultActions result = mvc.perform(put(pathIdDisable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
		
		checkExpectedProperties(result, dto);

		verify(service).disable(dto.getId());
		verifyNoMoreInteractions(service);
	}

	@Test
	public void disableClubNotFound() throws Exception {
		final DTO dto = getNewDto();

		when(service.disable(dto.getId()))
				.thenThrow(new EntityNotFoundException(dto.getId(), "Code", "Message", "Developer Info"));

		mvc.perform(put(pathIdDisable, dto.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

		verify(service).disable(dto.getId());
		verifyNoMoreInteractions(service);
	}
}
