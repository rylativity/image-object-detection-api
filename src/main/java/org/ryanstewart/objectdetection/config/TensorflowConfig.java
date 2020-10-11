package org.ryanstewart.objectdetection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.ryanstewart.objectdetection.service.TensorflowService;
import org.ryanstewart.objectdetection.service.TensorflowServiceImpl;

@Configuration
public class TensorflowConfig {
	@Bean
	public TensorflowService tensorflowService() {
		return new TensorflowServiceImpl();
	}

}
