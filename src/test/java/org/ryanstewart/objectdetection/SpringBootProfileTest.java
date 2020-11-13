package org.ryanstewart.objectdetection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SpringBootProfileTest {
	@Value("${spring.application.name}")
	String appName;

	@Test
	void test() {
		assertThat(appName).isEqualTo("image-object-detection-api");
	}
}
