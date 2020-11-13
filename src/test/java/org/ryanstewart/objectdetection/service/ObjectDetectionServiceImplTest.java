package org.ryanstewart.objectdetection.service;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ObjectDetectionServiceImplTest {

	@MockBean
	TensorflowService tensorflowService;

	MockMultipartFile mockMultipartFile;

	ObjectDetectionService objectDetectionService;

	@Before
	public void setUp() throws Exception {
		tensorflowService = Mockito.mock(TensorflowServiceImpl.class);
		this.objectDetectionService = new ObjectDetectionServiceImpl(tensorflowService);
	}

	@Test
	public void shouldReturnSuccessfullyWhenCalledWithValidMultipartFile() throws Exception {
		InputStream is = this.getClass()
				.getClassLoader()
				.getResourceAsStream("testimage1.jpg");
		mockMultipartFile = new MockMultipartFile("image", "testimage1.jpg", "application/jpeg", is);

		when(tensorflowService.detectObjectsInImage(any(byte[].class)))
				.thenReturn(DetectionResponseDTO.builder().build());
		DetectionResponseDTO detectionResponseDTO = objectDetectionService.detectAll(mockMultipartFile);
	}

	@Test(expected = RestClientException.class)
	public void shouldThrowExceptionWhenCalledWithNullMultipartFile() throws Exception {
		objectDetectionService.detectAll(null);
	}

	@Test(expected = RestClientException.class)
	public void shouldThrowExceptionWhenCalledWithNullContentMultipartFile() throws Exception {
		InputStream is = null;
		mockMultipartFile = new MockMultipartFile("image", "testimage1.jpg", "application/jpeg", is);
		objectDetectionService.detectAll(mockMultipartFile);
	}
}