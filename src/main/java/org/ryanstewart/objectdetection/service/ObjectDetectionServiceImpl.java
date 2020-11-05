package org.ryanstewart.objectdetection.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOException;
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
	public DetectionResponseDTO detectAll(MultipartFile img) throws Exception {

		return runObjectDetection(img);
	}

	private DetectionResponseDTO runObjectDetection(MultipartFile img) throws Exception {

		if (img == null) {
			LOG.error("MultipartFile IMAGE IS NULL IN OBJECTDETECTIONSERVICEIMPL!!!");
		}
		byte[] imgAsBytes = imageToByteArray(img);
		if (imgAsBytes == null) {
			LOG.error("IMAGE BYTES IS NULL!!!");
		}
		return detectObjectsFromImgByteArray(imgAsBytes);
	}

	private DetectionResponseDTO detectObjectsFromImgByteArray(byte[] imgAsBytes) throws Exception {

		return tensorflowService.detectObjectsInImage(imgAsBytes);
	}

	public byte[] imageToByteArray(MultipartFile img) throws IOException {
		BufferedImage bImage = ImageIO.read(img.getInputStream());
		BufferedImage jpegImage;
		try {
			jpegImage = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		} catch (NullPointerException ex) {
			throw new IIOException("Not a valid image file");
		}

		jpegImage.createGraphics().drawImage(bImage, 0, 0, Color.BLACK, null);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(jpegImage, "jpg", bos);
		return bos.toByteArray();
	}

}
