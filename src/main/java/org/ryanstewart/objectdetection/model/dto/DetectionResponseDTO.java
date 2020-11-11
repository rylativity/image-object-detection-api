package org.ryanstewart.objectdetection.model.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@ApiModel(description = "Class representing object detection results")
@Log4j2
//TODO figure out how to unpack this response object without hardcoding tensor labels. Consider not making this a defined class for flexibility
public class DetectionResponseDTO implements Serializable {
	@ApiModelProperty(notes = "Detection Boxes Generated By Model")
	private List<List<Float>> boundingBoxes;

	@ApiModelProperty(notes = "Detection Certainty Scores Generated by Model for each class and bounding box")
	private List<Float> scores;

	@ApiModelProperty(notes = "Detection Classes Identified in each bounding box as a (int) float value, certainty of class prediction tracked in detectionScores")
	@JsonIgnore
	private List<Integer> classNums;

	@ApiModelProperty(notes = "The named class of each detection")
	private List<String> classes;

	@ApiModelProperty(notes = "Total number of detections made in the image")
	//float instead of int for compatibility with most models
	private int numDetections;

//	@ApiModelProperty(notes = "Object holding lists of classification values for pre-specified threshold certainty values")
//	Map<String, Map<String, List<List<?>>>> thresholdDetections;

	@ApiModelProperty(notes = "Map where keys are classnames and values are arrays of floats")
	Map<String, List<Float>> classConfidences;

	//TODO Add Merge Method
	public void merge(DetectionResponseDTO otherDetectionResponseDto) {
		if (otherDetectionResponseDto.getNumDetections() != 0) {
			LOG.info("Merging " + otherDetectionResponseDto.getNumDetections() + " detections into response");
			System.out.println(otherDetectionResponseDto.getBoundingBoxes().toString());
			this.boundingBoxes.addAll(otherDetectionResponseDto.getBoundingBoxes());
			this.scores.addAll(otherDetectionResponseDto.getScores());
			this.classNums.addAll(otherDetectionResponseDto.getClassNums());
			this.classes.addAll(otherDetectionResponseDto.getClasses());
			this.numDetections += otherDetectionResponseDto.getNumDetections();

			List<Float> confidences;
			for (String s : otherDetectionResponseDto.getClassConfidences().keySet()) {
				confidences = otherDetectionResponseDto.getClassConfidences().get(s);
				if (this.classConfidences.keySet().contains(s)) {
					this.classConfidences.get(s).addAll(confidences);
				} else {
					this.classConfidences.put(s, confidences);
				}
			}
		}
	}
}
