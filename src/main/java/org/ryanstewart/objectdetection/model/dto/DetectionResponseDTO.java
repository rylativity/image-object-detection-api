package org.ryanstewart.objectdetection.model.dto;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(description = "Class representing object detection results")
public class DetectionResponseDTO {
	@ApiModelProperty(notes = "Map results of the object detection models")
	public Map<String, Object> detectionResults;

	@Builder
	public DetectionResponseDTO(Map<String, Object> detectionResults) {
		this.detectionResults = detectionResults;
	}
}
