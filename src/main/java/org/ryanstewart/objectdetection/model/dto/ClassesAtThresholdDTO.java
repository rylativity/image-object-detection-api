package org.ryanstewart.objectdetection.model.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@ApiModel(description = "Class representing object detection results")
public class ClassesAtThresholdDTO
{
	@ApiModelProperty(notes = "Named classes identified at 90% certainty threshold")
	List<String> classesAt90;

	@ApiModelProperty(notes = "Named classes identified at 75% certainty threshold")
	List<String> classesAt75;

	@ApiModelProperty(notes = "Named classes identified at 60% certainty threshold")
	List<String> classesAt60;

	@ApiModelProperty(notes = "Named classes identified at 50% certainty threshold")
	List<String> classesAt50;

	public Map<String, List<String>> toMap(){
		Map<String, List<String>> map = new HashMap<>();
		map.put("90", classesAt90);
		map.put("75", classesAt75);
		map.put("60", classesAt60);
		map.put("50", classesAt50);
		return map;
	}

	public String toJson() throws JsonProcessingException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(this.toMap());
	}

}
