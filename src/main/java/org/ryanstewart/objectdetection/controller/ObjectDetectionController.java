package org.ryanstewart.objectdetection.controller;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.ryanstewart.objectdetection.service.ObjectDetectionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "Perform object detection on binary image data", produces = MediaType.APPLICATION_JSON_VALUE)
@Log4j2
public class ObjectDetectionController {
	private final ObjectDetectionService service;

	public ObjectDetectionController(final ObjectDetectionService service) {
		this.service = service;
	}

	@PostMapping(value = "/detect", consumes = { "multipart/form-data" })
	@ApiOperation(value = "Get results of object detection model on input image", response = DetectionResponseDTO.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success", response = DetectionResponseDTO.class) })
	public DetectionResponseDTO detectAll(@RequestParam(name = "image") MultipartFile uploadedImage)
			throws Exception {
		if (uploadedImage == null) {
			LOG.info("No Image Content Found In Request");
			throw new RestClientException(
					"Must set Header 'Content-Type: multipart/form-data' and provide an image in 'image' field of request") {
			};
		} else {
			LOG.info("Image request uploaded successfully.  Processing image with filename: "
					+ uploadedImage.getOriginalFilename());
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
