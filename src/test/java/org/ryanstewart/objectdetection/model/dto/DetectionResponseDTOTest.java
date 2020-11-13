package org.ryanstewart.objectdetection.model.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class DetectionResponseDTOTest {

	@Test
	public void testMerge() {
		DetectionResponseDTO thisDto = DetectionResponseDTO.builder()
				.classNums(new ArrayList<>(Arrays.asList(1, 1)))
				.classes(new ArrayList<>(Arrays.asList("Person", "Person")))
				.boundingBoxes(new ArrayList<>(
						Arrays.asList(Arrays.asList(.1f, .2f, .3f, .4f), Arrays.asList(.1f, .2f, .3f, .4f))))
				.scores(new ArrayList<>(Arrays.asList(.99f, .98f)))
				.classConfidences(new HashMap<>(Collections.singletonMap("Person", Arrays.asList(.99f, .98f))))
				.numDetections(2)
				.build();
		DetectionResponseDTO thatDto = DetectionResponseDTO.builder()
				.classNums(new ArrayList<>(Arrays.asList(2, 2)))
				.classes(new ArrayList<>(Arrays.asList("Bicycle", "Bicycle")))
				.boundingBoxes(new ArrayList<>(
						Arrays.asList(Arrays.asList(.1f, .2f, .3f, .4f), Arrays.asList(.1f, .2f, .3f, .4f))))
				.scores(new ArrayList<>(Arrays.asList(.97f, .96f)))
				.classConfidences(new HashMap<>(Collections.singletonMap("Bicycle", Arrays.asList(.97f, .96f))))
				.numDetections(2)
				.build();

		thisDto.merge(thatDto);

		assert thisDto.getNumDetections() == 4;
		assert thisDto.getClasses().contains("Person");
		assert thisDto.getClasses().contains("Bicycle");
		assert thisDto.getClassConfidences().containsKey("Person");
		assert thisDto.getClassConfidences().containsKey("Bicycle");
		assert thisDto.getBoundingBoxes().size() == 4;
	}
}