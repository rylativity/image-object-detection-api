package org.ryanstewart.objectdetection.service;

import java.io.IOException;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ObjectDetectionService {
	DetectionResponseDTO detectAll(final MultipartFile accessCardId) throws IOException;
}
