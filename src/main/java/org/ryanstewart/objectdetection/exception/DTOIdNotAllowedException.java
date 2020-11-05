package org.ryanstewart.objectdetection.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DTOIdNotAllowedException extends BaseEntityNotFoundException {
	public DTOIdNotAllowedException(Long id, String dataName) {
		super(String.format("DTO cannot have an Id. Id %d for %s data type", id, dataName));
	}
}
