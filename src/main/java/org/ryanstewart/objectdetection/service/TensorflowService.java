package org.ryanstewart.objectdetection.service;

import java.io.IOException;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;

public interface TensorflowService {
	DetectionResponseDTO detectObjectsInImage(byte[] imgAsBytes) throws IOException;
}
