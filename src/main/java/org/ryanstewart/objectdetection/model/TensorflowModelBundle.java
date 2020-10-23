package org.ryanstewart.objectdetection.model;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import lombok.NoArgsConstructor;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class TensorflowModelBundle {

	String boxesMapKey = "boundingBoxes";
	String classNumsMapKey = "classNums";
	String scoresMapKey = "scores";
	String numDetectionsMapKey = "numDetections";

	String[] expectedConfigKeys = new String[] { boxesMapKey, classNumsMapKey, scoresMapKey, numDetectionsMapKey };

	float[] defaultThresholds;

	double minConfidence;

	private SavedModelBundle model;

	private TensorDescriptor inputTensor;

	private List<TensorDescriptor> outputTensors;

	private Map<Integer, String> classNameDictionary;

	public TensorflowModelBundle(Map<String, Object> configMap) throws Exception {

		String modelPath = (String) configMap.get("modelPath");
		if (modelPath == null) {
			throw new Exception("Key 'modelPath' expected but not found in configuration file");
		}
		this.model = SavedModelBundle.load(modelPath, "serve");

		Map<String, Object> inputTensorConfig = (Map<String, Object>) configMap.get("inputTensor");
		if (inputTensorConfig == null) {
			throw new Exception("Key 'inputTensor' expected but not found in configuration file");
		}
		String inputTensorLabel = (String) inputTensorConfig.get("label");
		String inputTensorType = (String) inputTensorConfig.get("type");
		int inputTensorDimensions = (int) inputTensorConfig.get("ndim");
		if (inputTensorLabel == null || inputTensorType == null || inputTensorDimensions == 0) {
			throw new Exception(
					"Keys 'label'(string), 'type'(string), and 'ndim'(int) expected but not found under key 'inputTensor in configuration file");
		}
		this.inputTensor = new TensorDescriptor(inputTensorDimensions, inputTensorLabel, inputTensorType, "input");

		Map<String, Object> outputTensorConfigs = (Map<String, Object>) configMap.get("outputTensors");
		if (outputTensorConfigs == null) {
			throw new Exception("Key 'outputTensors' expected but not found in configuration file");
		}
		Map<String, Object> outputTensorConfig;
		String outputTensorLabel;
		String outputTensorType;
		int outputTensorDimensions;
		this.outputTensors = new ArrayList<>();
		for (String configKey : expectedConfigKeys) {
			outputTensorConfig = (Map<String, Object>) outputTensorConfigs.get(configKey);
			outputTensorLabel = (String) outputTensorConfig.get("label");
			outputTensorType = (String) outputTensorConfig.get("type");
			outputTensorDimensions = (int) outputTensorConfig.get("ndim");
			if (outputTensorLabel == null || outputTensorType == null || outputTensorDimensions == 0) {
				throw new Exception(
						"Keys 'label'(string), 'type'(string), and 'ndim'(int) expected but not found under key 'outputTensors."
								+ configKey + " in configuration file");
			}
			this.outputTensors.add(
					new TensorDescriptor(outputTensorDimensions, outputTensorLabel, outputTensorType, configKey));
		}

		this.classNameDictionary = (Map<Integer, String>) configMap.get("classNameDictionary");
		if (classNameDictionary == null) {
			throw new Exception("Key 'classNameDictionary' expected but not found in configuration file");
		}

//		String[] defaultThresholdStrings = ((String) configMap.get("confidenceThresholds")).split(",");
//		defaultThresholds = new float[defaultThresholdStrings.length];
//		float floatVal;
//		for (int i = 0; i < defaultThresholdStrings.length; i++) {
//			floatVal = Float.parseFloat(defaultThresholdStrings[i]);
//			if (floatVal >= 1.0 || floatVal < 0.0) {
//				throw new Exception("confidenceThresholds must all be less than 1.0 and greater than 0.0");
//			}
//			defaultThresholds[i] = floatVal;
//		}

		this.minConfidence = (double) configMap.get("minConfidence");
	}

	// TODO
	public void preprocessImageForInference(final byte[] imgAsBytes) {

	}

	public DetectionResponseDTO detectObjectsInImage(final byte[] imgAsBytes) throws Exception {
		Tensor<UInt8> inputTensorImage = makeImageTensor(imgAsBytes);

		Session.Runner runner = model.session().runner()
				.feed(inputTensor.getLabel(), inputTensorImage);

		for (TensorDescriptor outputTensor : outputTensors) {
			runner.fetch(outputTensor.getLabel());
		}
		List<Tensor<?>> result = runner.run();
		inputTensorImage.close();

		Map<String, Object> mapResults = unpackTensorflowResults(result);
		for (Tensor t : result) {
			t.close();
		}

		return DetectionResponseDTO.builder()
				.scores((List<Float>) mapResults.get(scoresMapKey))
				.boundingBoxes((List<List<Float>>) mapResults.get(boxesMapKey))
				.classNums((List<Integer>) mapResults.get(classNumsMapKey))
				.numDetections(((Float) mapResults.get(numDetectionsMapKey)).intValue())
				.classes((List<String>) mapResults.get("classes"))
//				.thresholdDetections((Map<String, Map<String, List<List<?>>>>) mapResults.get("thresholdDetections"))
				.classConfidences((Map<String, List<Float>>) mapResults.get("classConfidences"))
				.build();
	}

	//TODO update thresholdvaluecalculator to take values from controller params
	private Map<String, List<List<Float>>> identifyCategoriesOverThreshold(Map<String, Object> results,
			float threshold) {
		List<Float> scores = (List<Float>) results.get(scoresMapKey);
		List<String> categories = (List<String>) results.get("classes");
		Map<String, List<List<Float>>> catsOverThresh = new HashMap<>();
		String categoryName;
		Float score;
		for (int i = 0; i < scores.size(); i++) {
			score = scores.get(i);
			if (score >= threshold) {
				//Get label name to use as map key
				categoryName = categories.get(i);

				//Retrieve List<List<Float>> holding bounding boxes and store in map with label as map key
				List<List<Float>> swap;
				if (catsOverThresh.containsKey(categoryName)) {
					swap = catsOverThresh.get(categoryName);
					swap.add(((List<List<Float>>) results.get(boxesMapKey)).get(i));
					catsOverThresh.put(categoryName, swap);
				} else {
					swap = new ArrayList<>();
					swap.add(((List<List<Float>>) results.get(boxesMapKey)).get(i));
					catsOverThresh.put(categoryName, swap);
				}
			}
		}
		return catsOverThresh;
	}

	private Map<String, Object> unpackTensorflowResults(List<Tensor<?>> resultTensors) throws Exception {
		int numResultTensors = resultTensors.size();
		try {
			assert numResultTensors == this.outputTensors.size();
		} catch (AssertionError aE) {
			throw new IOException("Size of resultTensors must equal size of outputTensorLabels");
		}

		Map<String, Object> map = new HashMap<>();
		int dims;
		Tensor tensor;
		TensorDescriptor tensorObject;
		for (int i = 0; i < outputTensors.size(); i++) {
			tensorObject = outputTensors.get(i);
			String configType = tensorObject.getConfigType().replace(":0", "");
			String outputTensorType = tensorObject.getType();
			tensor = resultTensors.get(i);
			dims = tensor.numDimensions();
			if (dims != tensorObject.getNdim()) {
				throw new Exception("Dimensions provided in configuration file for output tensor " +
						configType + " does not match tensor found with " + dims + "dimensions");
			}

			switch (dims) { // TODO These cases assume that a single input tensor and one of each output tensors are used.
			// Currently assumes that the first value in tensor.shape() ALWAYS == 1
			// (in other words, contains result tensors for single input tensor
			//                          with first value in tensor.shape() always == 1)

			case 1: //Handle scalar tensor
				// When tensor.numDimensions() == 1; e.g. [4] with shape [1]
				switch (outputTensorType.toLowerCase()) {
				case "float":
					float floatVal = unpackScalarFloatTensor(tensor);
					map.put(configType, floatVal);
					break;
				case "int":
				case "integer":
					int intVal = unpackScalarIntegerTensor(tensor);
					map.put(configType, intVal);
					break;
				default:
					throw new IOException("Output Tensor Type " + outputTensorType + " not supported");
				}
				break;

			case 2:
				// When tensor.numDimensions() == 2; e.g. [1,4,5,9] with shape [1, 4]
				switch (outputTensorType.toLowerCase()) {
				case "float":
					List<Float> oneDimFloatList = unpack1dFloatTensor(tensor);
					map.put(configType, oneDimFloatList);
					break;
				case "int":
				case "integer":
					List<Integer> oneDimIntegerList = unpack1dIntegerTensor(tensor);
					map.put(configType, oneDimIntegerList);
					break;
				default:
					throw new IOException("Output Tensor Type " + outputTensorType + " not supported");
				}
				break;
			case 3:
				// When tensor.numDimensions() == 3; e.g. [[1,4,5,9],[8,0,1,3]] with shape [1, 2, 4]
				switch (outputTensorType.toLowerCase()) {
				case "float":
					List<List<Float>> twoDimFloatArray = unpack2dFloatTensor(tensor);
					map.put(configType, twoDimFloatArray);
					break;
				case "int":
				case "integer":
					List<List<Integer>> twoDimIntegerArray = unpack2dIntegerTensor(tensor);
					map.put(configType, twoDimIntegerArray);
					break;
				default:
					throw new IOException("Output Tensor Type " + outputTensorType + " not supported");
				}
				break;
			default:
				throw new IOException("No unpacking handler for tensor with " + dims + " dimensions");
			}
			tensor.close();
		}
		if (minConfidence != 0.0) {
			pruneBelowThreshold(map);
		}
		addClassNames(map);
//		addThresholdClassifications(map);
		addClassConfidences(map);

		return map;
	}

	// When tensor.numDimensions() == 1; e.g. [4] with shape [1,]
	private float unpackScalarFloatTensor(Tensor tensor) {
		float[] oneDimFloatArray = new float[(int) tensor.numElements()];
		tensor.copyTo(oneDimFloatArray);
		return oneDimFloatArray[0];
	}

	private int unpackScalarIntegerTensor(Tensor tensor) {
		int[] oneDimFloatArray = new int[(int) tensor.numElements()];
		tensor.copyTo(oneDimFloatArray);
		return oneDimFloatArray[0];
	}

	// When tensor.numDimensions() == 2; e.g. [1,4,5,9] with shape [1, 4]
	private List<Float> unpack1dFloatTensor(Tensor tensor) {
		float[][] twoDimFloatArray = new float[(int) tensor.shape()[0]][(int) tensor.shape()[1]];
		tensor.copyTo(twoDimFloatArray);
		List<Float> floatValues = new ArrayList<>();
		for (float f : twoDimFloatArray[0]) { //Handle single array tensor (TODO assuming single output tensor)
			floatValues.add(f);
		}
		return floatValues;
	}

	private List<Integer> unpack1dIntegerTensor(Tensor tensor) {
		int[][] twoDimIntegerArray = new int[(int) tensor.shape()[0]][(int) tensor.shape()[1]];
		tensor.copyTo(twoDimIntegerArray);
		List<Integer> floatValues = new ArrayList<>();
		for (int i : twoDimIntegerArray[0]) { //Handle single array tensor (TODO assuming single output tensor)
			floatValues.add(i);
		}
		return floatValues;
	}

	// When tensor.numDimensions() == 3; e.g. [[1,4,5,9],[8,0,1,3]] with shape [1, 2, 4]
	private List<List<Float>> unpack2dFloatTensor(Tensor tensor) {
		float[][][] threeDimFloatArray = new float[(int) tensor.shape()[0]][(int) tensor.shape()[1]][(int) tensor
				.shape()[2]];
		tensor.copyTo(threeDimFloatArray);
		List<List<Float>> twoDimFloatList = new ArrayList<>();
		for (float[] fArr : threeDimFloatArray[0]) //Handle array of array tensor (TODO assuming single output tensor)
		{
			List<Float> floatList2 = new ArrayList<>();
			for (float f : fArr) {
				floatList2.add(f);
			}
			twoDimFloatList.add(floatList2);
		}
		return twoDimFloatList;
	}

	private List<List<Integer>> unpack2dIntegerTensor(Tensor tensor) {
		int[][][] threeDimIntegerArray = new int[(int) tensor.shape()[0]][(int) tensor.shape()[1]][(int) tensor
				.shape()[2]];
		tensor.copyTo(threeDimIntegerArray);
		List<List<Integer>> twoDimIntegerList = new ArrayList<>();
		for (int[] intArr : threeDimIntegerArray[0]) //Handle array of array tensor (TODO assuming single output tensor)
		{
			List<Integer> intList = new ArrayList<>();
			for (int i : intArr) {
				intList.add(i);
			}
			twoDimIntegerList.add(intList);
		}
		return twoDimIntegerList;
	}

	private void addClassNames(Map<String, Object> detectionResponse) {
		List<?> _numericCategories = (List<?>) detectionResponse.get(classNumsMapKey);
		String typeName = _numericCategories.get(0).getClass().getSimpleName();
		List<Integer> numericCategories = new ArrayList<>();
		if (typeName.equals("Integer")) {
			numericCategories = (List<Integer>) _numericCategories;
		} else if (typeName.equals("Float")) {
			for (Object f : _numericCategories) {
				numericCategories.add(((Float) f).intValue());
			}
		}
		List<String> classNamesList = new ArrayList<>();
		String categoryName;
		for (Integer i : numericCategories) {
			categoryName = this.classNameDictionary.get(i);
			classNamesList.add(categoryName);
		}
		detectionResponse.put("classes", classNamesList);
	}

	private void addThresholdClassifications(Map<String, Object> detectionResponse) {

		Map<String, Object> map = new HashMap<>();
		String key;
		Map<String, List<List<Float>>> boxes;
		for (float f : defaultThresholds) {
			//Create key based on float value (e.g. 0.75 -> "75")
			key = Float.toString(f * 100f).replaceAll("\\.\\d+", "");
			boxes = identifyCategoriesOverThreshold(detectionResponse, f);
			map.put(key, boxes);
		}
		detectionResponse.put("thresholdDetections", map);
	}

	private void addClassConfidences(Map<String, Object> detectionResponse) {

		List<String> classNames = (List<String>) detectionResponse.get("classes");
		List<Float> scores = (List<Float>) detectionResponse.get(scoresMapKey);
		Map<String, List<?>> map = new HashMap<>();
		Set<String> classSet = new HashSet<>(classNames);

		List<Float> classScores;
		Float score;
		for (String uniqueClassName : classSet) {

			classScores = new ArrayList<>();
			for (int i = 0; i < classNames.size(); i++) {
				if (classNames.get(i).equals(uniqueClassName)) {
					classScores.add(scores.get(i));
				}
			}
			map.put(uniqueClassName, classScores);

		}
		detectionResponse.put("classConfidences", map);
	}

	//TODO FINISH THIS METHOD
	private void pruneBelowThreshold(Map<String, Object> map) {
		List<Float> scoresCurrent = (List<Float>) map.get(scoresMapKey);
		List<Integer> indicesBelowThresholdScore = new ArrayList<>();
		for (int i = 0; i < scoresCurrent.size(); i++) {
			if (scoresCurrent.get(i) < minConfidence) {
				indicesBelowThresholdScore.add(i);
			}
		}
		if (!indicesBelowThresholdScore.isEmpty()) {
			List<List<Float>> boxesCurrent = (List<List<Float>>) map.get(boxesMapKey);
			List<Float> classNumsCurrent = (List<Float>) map.get(classNumsMapKey);
			float numDetections = (float) map.get(numDetectionsMapKey);

			List<Float> scoresSwap = new ArrayList<>();
			List<List<Float>> boxesSwap = new ArrayList<>();
			List<Float> classNumsSwap = new ArrayList<>();
			for (int i = 0; i < indicesBelowThresholdScore.size(); i++) {
				if (!indicesBelowThresholdScore.contains(i)) {
					scoresSwap.add(scoresCurrent.get(i));
					boxesSwap.add(boxesCurrent.get(i));
					classNumsSwap.add(classNumsCurrent.get(i));
				}
			}
			numDetections -= (float) indicesBelowThresholdScore.size();
			map.put(scoresMapKey, scoresSwap);
			map.put(boxesMapKey, boxesSwap);
			map.put(classNumsMapKey, classNumsSwap);
			map.put(numDetectionsMapKey, numDetections);
		}
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
