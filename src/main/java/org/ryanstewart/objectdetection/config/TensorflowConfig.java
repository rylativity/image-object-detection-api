package org.ryanstewart.objectdetection.config;

import org.ryanstewart.objectdetection.model.TensorflowModelBundle;
import org.ryanstewart.objectdetection.service.TensorflowService;
import org.ryanstewart.objectdetection.service.TensorflowServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class TensorflowConfig {

	@Value("${tensorflow.config}")
	private String tensorflowConfig;

	@Bean
	public TensorflowService tensorflowService() throws Exception {
		TensorflowServiceImpl tfImpl = new TensorflowServiceImpl();
		//TODO get rid of Yaml lib and use spring properties directly
		Yaml yaml = new Yaml();
		InputStream is = this.getClass()
				.getClassLoader()
				.getResourceAsStream(tensorflowConfig);
		//Expected to contain one root-level key per model
		Map<String, Object> configMap = yaml.load(is);
		List<TensorflowModelBundle> modelBundles = new ArrayList<>();
		for (String k : configMap.keySet()) {
			if (!k.startsWith("model")) {
				throw new Exception(
						"Root keys of configuration yaml must match pattern 'model\\d+' (e.g. model0, model1, etc)");
			}
			modelBundles.add(new TensorflowModelBundle((Map<String, Object>) configMap.get(k)));
			System.out.println("Loaded Tensorflow Model " + k + " Defined In " + tensorflowConfig);
		}
		tfImpl.setModels(modelBundles);
		System.out.println(modelBundles.size() + " MODEL(S) LOADED FROM CONFIG FILE");
		return tfImpl;
	}

}
