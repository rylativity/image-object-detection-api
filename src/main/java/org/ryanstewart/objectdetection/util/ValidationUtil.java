package org.ryanstewart.objectdetection.util;

import org.ryanstewart.objectdetection.exception.DTOIdNotAllowedException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {
	@SuppressWarnings("unused")
	private void isNullOrThrow(Long id, String dataType) {
		if (id != null) {
			throw new DTOIdNotAllowedException(id, dataType);
		}
	}
}
