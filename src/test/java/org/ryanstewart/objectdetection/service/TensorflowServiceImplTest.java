package org.ryanstewart.objectdetection.service;

import org.ryanstewart.objectdetection.model.TensorflowModelBundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
public class TensorflowServiceImplTest {

	byte[] bytesImg;

	@Autowired
	TensorflowService tensorflowService;

	@TestConfiguration
	public static class TensorflowTestConfig {
		@Bean
		public TensorflowService tensorflowService() throws Exception {
			String tensorflowConfig = "object-detection-model-config.yml";
			TensorflowServiceImpl tfImpl = new TensorflowServiceImpl();
			//TODO get rid of Yaml lib and use spring properties directly
			Yaml yaml = new Yaml();
			InputStream is = this.getClass()
					.getClassLoader()
					.getResourceAsStream(tensorflowConfig);
			//Expected to contain one root-level key per model
			Map<String, Object>         configMap    = yaml.load(is);
			List<TensorflowModelBundle> modelBundles = new ArrayList<>();
			for (String k : configMap.keySet()) {
				if (!k.startsWith("model")) {
					throw new Exception(
							"Root keys of " + tensorflowConfig
									+ " must match pattern 'model\\d+' (e.g. model0, model1, etc)");
				}
				modelBundles.add(new TensorflowModelBundle((Map<String, Object>) configMap.get(k)));
			}
			tfImpl.setModels(modelBundles);
			return tfImpl;
		}
	}

	@Before
	public void setUp() throws Exception {
		InputStream is = this.getClass()
				.getClassLoader()
				.getResourceAsStream("testimage1.jpg");
		BufferedImage img = ImageIO.read(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		baos.flush();
		this.bytesImg = baos.toByteArray();
		baos.close();
	}

	@Test
	public void detectObjectsInImage() throws Exception {
		tensorflowService.detectObjectsInImage(bytesImg);
	}
}