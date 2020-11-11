package org.ryanstewart.objectdetection.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.ryanstewart.objectdetection.service.ObjectDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(ObjectDetectionController.class)
@ActiveProfiles("test")
public class ObjectDetectionControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ObjectDetectionService service;

	@Test
	public void shouldReturnDtoWhenCalledSuccessfully() throws Exception {
		InputStream is = this.getClass()
				.getClassLoader()
				.getResourceAsStream("testimage1.jpg");
		MockMultipartFile imageFile = new MockMultipartFile("image", "testimage1.jpg", "multipart/form-data", is);

		when(service.detectAll(imageFile)).thenReturn(DetectionResponseDTO.builder().build());

		this.mvc.perform(MockMvcRequestBuilders.multipart("/api/detect").file(imageFile)
				.contentType("multipart/form-data"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldThrowClientErrorWhenCalledWithNonImageFile() throws Exception {
		InputStream is = null;

		MockMultipartFile notImageFile = new MockMultipartFile("image", "banner.txt", "multipart/form-data", is);

		this.mvc.perform(MockMvcRequestBuilders.multipart("/api/detect").file(notImageFile)
				.contentType("multipart/form-data"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	public void shouldAlwaysRespondHello() throws Exception
	{
		this.mvc.perform(MockMvcRequestBuilders.get("/api/helloworld"))
				.andExpect(status().isOk());
	}
}