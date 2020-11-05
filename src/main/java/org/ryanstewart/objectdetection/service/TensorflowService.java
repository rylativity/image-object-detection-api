package org.ryanstewart.objectdetection.service;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;

public interface TensorflowService {
	DetectionResponseDTO detectObjectsInImage(byte[] imgAsBytes) throws Exception;

	void preprocessImageForInference(byte[] imgAsBytes);
}
