package org.ryanstewart.objectdetection.service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

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

	SavedModelBundle model;
	String[] labels = labelsAsPropertyString.split(",");

	@Autowired
	public TensorflowServiceImpl() {
		System.out.println("MODEL NAME ::: ::: ::: " + modelPath);
		SavedModelBundle model = SavedModelBundle.load(modelPath, "serve");
		this.model = model;
		this.labels = labels;
	}

	@Override
	public DetectionResponseDTO detectObjectsInImage(final byte[] imgAsBytes) throws IOException {
		Tensor<UInt8> inputTensor = makeImageTensor(imgAsBytes);

		Session.Runner runner = model.session().runner()
				.feed("image_tensor:0", inputTensor);

		for(String label : labels){
			runner.fetch(label);
		}

		List<Tensor<?>> result = runner.run();

		Map<String, Object> resultMap = unpackTensorflowResults(result, labels);

		//TODO unpack results depending on format
		return new DetectionResponseDTO(resultMap);
	}

	private Map<String, Object> unpackTensorflowResults(List<Tensor<?>> resultTensors, String[] tensorLabels) throws IOException
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
			String label = tensorLabels[i];
			tensor = resultTensors.get(i);
			dims = tensor.numDimensions();
			List<Float> floats;

			switch(dims){
				case 0:
					float scalarTensorValue = tensor.floatValue();
					map.put(label, scalarTensorValue);
					break;
				case 1:
					float[] oneDimFloatArray = new float[(int) tensor.numElements()];
					tensor.copyTo(oneDimFloatArray);
					floats = new ArrayList<>();
					for(float f : oneDimFloatArray){
						floats.add(f);
					}
					map.put(label, floats);
					break;
				case 2:
					float[][] twoDimfloatArray = new float[(int)tensor.shape()[0]][(int) tensor.shape()[1]];
					tensor.copyTo(twoDimfloatArray);
					List<List<Float>> twoDimList = new ArrayList<>();
					for (float[] row:twoDimfloatArray){
						floats = new ArrayList<>();
						for(float f : row){
							floats.add(f);

						}
						twoDimList.add(floats);
					}
					map.put(label, twoDimList);
					break;
				case 3:
					float[][][] threeDimfloatArray = new float[(int)tensor.shape()[0]][(int) tensor.shape()[1]][(int) tensor.shape()[2]];
					tensor.copyTo(threeDimfloatArray);
					List<List<List<Float>>> threeDimList = new ArrayList<>();
					for (float[][] matrix:threeDimfloatArray)
					{
						List<List<Float>> twoDimList_2 = new ArrayList<>();
						for (float[] row : matrix)
						{
							floats = new ArrayList<>();
							for (float f : row)
							{
								floats.add(f);

							}
							twoDimList_2.add(floats);

						}
						threeDimList.add(twoDimList_2);
					}
					map.put(label, threeDimList);
					break;
				default:
					throw new IOException("No unpacking handler for tensor with " + dims + " dimensions");
			}
		}
		return map;
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
