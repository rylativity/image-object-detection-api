package org.ryanstewart.objectdetection.util;

import lombok.experimental.UtilityClass;

import org.ryanstewart.objectdetection.exception.DTOIdNotAllowedException;

@UtilityClass
public class ValidationUtil {
	@SuppressWarnings("unused")
	private void isNullOrThrow(Long id, String dataType) {
		if (id != null) {
			throw new DTOIdNotAllowedException(id, dataType);
		}
	}
}
