package org.giste.spring.server.controller.advice;

import org.giste.spring.server.service.exception.DuplicatedPropertyException;
import org.giste.spring.server.service.exception.EntityNotFoundException;
import org.giste.spring.util.error.dto.RestErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handles common exceptions coming from service layer.
 * 
 * @author Giste
 */
@ControllerAdvice
public class CrudeServiceExceptionHandler {

	/**
	 * Handles {@link EntityNotFoundException} and converts it in
	 * {@link RestErrorDto}.
	 * 
	 * @param e {@link EntityNotFoundException} to be handled.
	 * @return {@link RestErrorDto} to be returned to Rest client.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = EntityNotFoundException.class)
	@ResponseBody
	public RestErrorDto handleEntityNotFound(EntityNotFoundException e) {
		return new RestErrorDto(HttpStatus.NOT_FOUND, e.getCode(), e.getMessage(), e.getDeveloperInfo());
	}

	/**
	 * Handles the {@link DuplicatedPropertyException} thrown when a creating or
	 * updating an entity with a duplicated value in a property that has to be
	 * unique.
	 * 
	 * @param e {@link DuplicatedPropertyException} to be handled.
	 * @return {@link RestErrorDto} to be returned to Rest client.
	 */
	@ExceptionHandler(DuplicatedPropertyException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public RestErrorDto handleDuplicatedPropertyException(DuplicatedPropertyException e) {
		return new RestErrorDto(HttpStatus.CONFLICT, e.getCode(), e.getMessage(), e.getDeveloperInfo());
	}
}
