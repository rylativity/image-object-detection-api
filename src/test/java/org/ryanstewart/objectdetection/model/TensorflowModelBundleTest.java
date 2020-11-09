package org.ryanstewart.objectdetection.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;
import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.yaml.snakeyaml.Yaml;

public class TensorflowModelBundleTest {

	private TensorflowModelBundle tfmb;
	private byte[] imageAsBytes;

	@Before
	public void setup() throws Exception {
		//Load Model
		Yaml yaml = new Yaml();
		InputStream is = this.getClass()
				.getClassLoader()
				.getResourceAsStream("object-detection-model-config.yml");
		Map<String, Object> configMap = yaml.load(is);
		tfmb = new TensorflowModelBundle((Map<String, Object>) configMap.get("model1"));

		//Load Image
		is = this.getClass().getClassLoader().getResourceAsStream("testimage1.jpg");
		BufferedImage bimg = ImageIO.read(is);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bimg, "jpg", bos);
		imageAsBytes = bos.toByteArray();
	}

	@Test
	public void testDetectObjectsInImage() throws Exception {
		DetectionResponseDTO detectionResponseDTO = tfmb.detectObjectsInImage(imageAsBytes);
		System.out.println("ABC");
		int numDetections = detectionResponseDTO.getNumDetections();

		//Assert all arrays of correct size
		assert detectionResponseDTO.getScores().size() == numDetections;
		assert detectionResponseDTO.getBoundingBoxes().size() == numDetections;
		assert detectionResponseDTO.getClasses().size() == numDetections;

		//Assert min confidence being respected
		for (Float f : detectionResponseDTO.getScores()) {
			assert f > tfmb.minConfidence;
		}
	}
}