package org.ryanstewart.objectdetection.controller;

import java.io.IOException;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.ryanstewart.objectdetection.service.ObjectDetectionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.log4j.Log4j2;

/*
 * - Demonstrate one to one
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "Perform object detection on binary image data", produces = MediaType.APPLICATION_JSON_VALUE)
@Log4j2
public class ObjectDetectionController {
	private final ObjectDetectionService service;

	public ObjectDetectionController(final ObjectDetectionService service) {
		this.service = service;
	}

	@PostMapping(value = "/detect", consumes = { "application/png", "application/jpeg", "multipart/form-data" })
	@ApiOperation(value = "Get results of object detection model on input image", response = DetectionResponseDTO.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success", response = DetectionResponseDTO.class) })
	public DetectionResponseDTO detectAll(@RequestParam(name = "image") MultipartFile uploadedImage)
			throws IOException {
		if (uploadedImage == null) {
			System.out.println("Uploaded Image is null in Controller");
			return null;
		} else {
			System.out.println("Image request uploaded successfully");
		}
		return service.detectAll(uploadedImage);
	}

	@GetMapping(value = "/helloworld")
	@ApiOperation(value = "Test if controller is up and running", response = String.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success", response = String.class) })
	public String hello() {
		return "Hello From The Object Detection Controller!";
	}

}
