package org.ryanstewart.objectdetection.service;

import org.ryanstewart.objectdetection.model.TensorflowModelBundle;
import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class TensorflowServiceImpl implements TensorflowService {

	@Setter
	private List<TensorflowModelBundle> models;

	// TODO
	@Override
	public void preprocessImageForInference(final byte[] imgAsBytes) {

	}

	@Override
	public DetectionResponseDTO detectObjectsInImage(final byte[] imgAsBytes) throws Exception {
		List<DetectionResponseDTO> detectionResponseDtos = new ArrayList<>();
		for (TensorflowModelBundle model : models) {
			detectionResponseDtos.add(model.detectObjectsInImage(imgAsBytes));
		}
		DetectionResponseDTO response = null;
		if (!detectionResponseDtos.isEmpty()) {
			response = detectionResponseDtos.get(0);
			for (int i = 1; i < detectionResponseDtos.size(); i++) {
				response.merge(detectionResponseDtos.get(i));
			}
		}

		return response;
	}

//	private DetectionResponseDTO mergeDetectionResponses(List<DetectionResponseDTO> detectionResponseDTOs){
//		List<List<Float>> detectionBoxes = new ArrayList<>();
//		List<Float> detectionScores = new ArrayList<>();
//		List<Integer> detectionClasses = new ArrayList<>();
//		List<String> detectionClassNames = new ArrayList<>();
//		int numDetections = 0;
//		List<String> classNamesCertainty50 = new ArrayList<>();
//		ClassesAtThresholdDTO classesAtThresholds;
//
//	}

}
