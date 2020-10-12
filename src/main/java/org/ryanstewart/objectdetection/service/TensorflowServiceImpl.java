package org.ryanstewart.objectdetection.service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ryanstewart.objectdetection.model.dto.ClassesAtThresholdDTO;
import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class TensorflowServiceImpl implements TensorflowService {


//	@Value("${tensorflow.model.path}") TODO use value from application.yml
	private String modelPath = "/Users/Ryan/github/image-object-detection-api/tf_models/ssd_mobilenet_v1_coco_2017_11_17";
	private String labelsAsPropertyString = "detection_boxes:0,detection_classes:0,detection_scores:0,num_detections:0";
	private String labelMapPath = "/Users/Ryan/github/image-object-detection-api/tf_models/mscoco_category_map.json";
	private float[] thresholdValuesString = new float[5];


	SavedModelBundle model;
	String[] tensorLabels = labelsAsPropertyString.split(",");
	Map<String,String> labelMap;

	@Autowired
	public TensorflowServiceImpl() throws IOException
	{
		System.out.println("MODEL NAME ::: ::: ::: " + modelPath);
		SavedModelBundle model = SavedModelBundle.load(modelPath, "serve");
		this.labelMap = new ObjectMapper().readValue(new File(labelMapPath), HashMap.class);
		this.model = model;
	}

	@Override
	public DetectionResponseDTO detectObjectsInImage(final byte[] imgAsBytes) throws IOException {
		Tensor<UInt8> inputTensor = makeImageTensor(imgAsBytes);

		Session.Runner runner = model.session().runner()
				.feed("image_tensor:0", inputTensor);

		for(String label : tensorLabels){
			runner.fetch(label);
		}

		List<Tensor<?>> result = runner.run();

		DetectionResponseDTO detectionResponseDTO = unpackTensorflowResults(result, tensorLabels);

		//TODO unpack results depending on format
		return detectionResponseDTO;
	}

	private List<String> identifyCategoriesOverThreshold(DetectionResponseDTO results, float threshold)
	{
		List<Float> scores = (List<Float>) results.getDetectionScores(); // TODO check these casts
		List<Integer> numericCategories = (List<Integer>) results.getDetectionClasses(); // TODO check these casts
		HashSet<String> catsOverThreshWDuplicates = new HashSet<>();
		String conversionMapKey;
		String categoryName;
		Float score;
		for(int i=0; i < scores.size(); i++){
			score = scores.get(i);
			if(score >= threshold){
				//TODO grab detection_class and then convert value
				conversionMapKey = Float.toString(numericCategories.get(i)).replace(".0","");
				categoryName = labelMap.get(conversionMapKey);
				catsOverThreshWDuplicates.add(categoryName);
			}
		}
		List<String> catsOverThresh = new ArrayList<>(catsOverThreshWDuplicates);

		return catsOverThresh;
	}

	private DetectionResponseDTO unpackTensorflowResults(List<Tensor<?>> resultTensors, String[] tensorLabels) throws IOException
	// TODO update this to return list of Maps, one for each input
	{
		try {
			assert resultTensors.size() == tensorLabels.length;
		} catch ( AssertionError aE ) {
			throw new IOException("Size of resultTensors must equal size of tensorLabels");
	}

		Map<String, Object > map = new HashMap<>();
		int numResultTensors = resultTensors.size();
		int dims;
		Tensor tensor;
		for(int i=0; i < numResultTensors; i++){
			String label = tensorLabels[i].replace(":0","");
			tensor = resultTensors.get(i);
			dims = tensor.numDimensions();

			switch(dims){ // TODO These assume that a single input tensor and one of each output tensors are used.
				// TODO extrapolate to any number of input and output tensors
				case 0:
					System.out.println("One Dimensional tensor '" + label + "' encountered while unpacking results");
					break;
				case 1: //Handle scalar tensor (TODO assuming single output tensor)
					float[] oneDimFloatArray = new float[(int) tensor.numElements()];
					tensor.copyTo(oneDimFloatArray);
					float floatValue = oneDimFloatArray[0];
					map.put(label, floatValue);
					break;
				case 2:
					float[][] twoDimFloatArray = new float[(int)tensor.shape()[0]][(int) tensor.shape()[1]];
					tensor.copyTo(twoDimFloatArray);
					List<Float> floatValues = new ArrayList<>();
					for (float f:twoDimFloatArray[0]){ //Handle single array tensor (TODO assuming single output tensor)
						floatValues.add(f);
					}
					map.put(label, floatValues);
					break;
				case 3:
					float[][][] threeDimFloatArray = new float[(int)tensor.shape()[0]][(int) tensor.shape()[1]][(int) tensor.shape()[2]];
					tensor.copyTo(threeDimFloatArray);
					List<List<Float>> twoDimFloatList = new ArrayList<>();
					for (float[] fArr:threeDimFloatArray[0]) //Handle array of array tensor (TODO assuming single output tensor)
					{
						List<Float> floatList2 = new ArrayList<>();
						for (float f : fArr)
						{
							floatList2.add(f);
						}
						twoDimFloatList.add(floatList2);
					}
					map.put(label, twoDimFloatList);
					break;
				default:
					throw new IOException("No unpacking handler for tensor with " + dims + " dimensions");
			}
		}
		DetectionResponseDTO detectionResponseDTO = new DetectionResponseDTO(map);
		addClassNames(detectionResponseDTO);
		addThresholdClassifications(detectionResponseDTO);

		return detectionResponseDTO;
	}

	private void addClassNames(DetectionResponseDTO detectionResponse){
		detectionResponse.setDetectionClassNames(
				identifyCategoriesOverThreshold(detectionResponse, -1f));
	}

	private void addThresholdClassifications(DetectionResponseDTO detectionResponse){

		ClassesAtThresholdDTO classesAtThresholds = new ClassesAtThresholdDTO();
		classesAtThresholds.setClassesAt50(identifyCategoriesOverThreshold(detectionResponse, 0.5f));
		classesAtThresholds.setClassesAt60(identifyCategoriesOverThreshold(detectionResponse, 0.6f));
		classesAtThresholds.setClassesAt75(identifyCategoriesOverThreshold(detectionResponse, 0.75f));
		classesAtThresholds.setClassesAt90(identifyCategoriesOverThreshold(detectionResponse, 0.9f));
		detectionResponse.setClassesAtThresholds(classesAtThresholds);
	}

	private static Tensor<UInt8> makeImageTensor(byte[] imageBytes) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
		BufferedImage img = ImageIO.read(is);

		if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			throw new IllegalArgumentException(
					String.format("Expected 3-byte BGR encoding in BufferedImage, found %d", img.getType()));
		}
		byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
		// ImageIO.read produces BGR-encoded images, while the model expects RGB.
		bgrToRgb(data);

		//Expand dimensions since the model expects images to have shape: [1, None, None, 3]
		long[] shape = new long[] { 1, img.getHeight(), img.getWidth(), 3 }; //TODO Remove Hard Coded Dimensions here

		return Tensor.create(UInt8.class, shape, ByteBuffer.wrap(data));
	}

	private static void bgrToRgb(byte[] data) {
		for (int i = 0; i < data.length; i += 3) {
			byte tmp = data[i];
			data[i] = data[i + 2];
			data[i + 2] = tmp;
		}
	}
}
