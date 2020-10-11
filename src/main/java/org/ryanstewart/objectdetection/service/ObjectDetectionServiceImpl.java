package org.ryanstewart.objectdetection.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.ryanstewart.objectdetection.model.dto.DetectionResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ObjectDetectionServiceImpl implements ObjectDetectionService {

	TensorflowService tensorflowService;

	@Autowired
	public ObjectDetectionServiceImpl(TensorflowService tensorflowService) {
		this.tensorflowService = tensorflowService;
	}

	@Override
	public DetectionResponseDTO detectAll(MultipartFile img) throws IOException {

		return runObjectDetection(img);
	}

	private DetectionResponseDTO runObjectDetection(MultipartFile img) throws IOException {
		System.out.println("TENSORFLOW SERVICE CLASS NAME : : : : " + tensorflowService.getClass().getName());
		if (img == null) {
			System.out.println("MultipartFile IMAGE IS NULL IN OBJECTDETECTIONSERVICEIMPL!!!");
		}
		byte[] imgAsBytes = img.getBytes();
		if (imgAsBytes == null) {
			System.out.println("IMAGE BYTES IS NULL!!!");
		}
		return detectObjectsFromImgByteArray(imgAsBytes);
	}

	private DetectionResponseDTO detectObjectsFromImgByteArray(byte[] imgAsBytes) throws IOException {

		return tensorflowService.detectObjectsInImage(imgAsBytes);
	}

	public byte[] imageToByteArray(MultipartFile img) throws Exception {
		BufferedImage bImage = ImageIO.read(img.getInputStream());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bImage, img.getContentType().replace("application/", ""), bos);
		return bos.toByteArray();
	}

}
