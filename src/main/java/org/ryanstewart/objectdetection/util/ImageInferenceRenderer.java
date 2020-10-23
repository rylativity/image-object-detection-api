package org.ryanstewart.objectdetection.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class ImageInferenceRenderer extends JFrame {

	@Setter
	private List<String> classNames;
	@Setter
	private List<List<Float>> boundingBoxes;

	private BufferedImage image;

	public void setImage(byte[] bimg) throws IOException {
		InputStream is = new ByteArrayInputStream(bimg);
		this.image = ImageIO.read(is);
	}

	public void setImage(BufferedImage bufImage) {
		this.image = bufImage;
	}

	//TODO
	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters
	}

	//TODO
	public void displayImage(BufferedImage bufImg) {

		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(bufImg)));
		frame.pack();
		frame.setVisible(true);
	}

	//TODO
	public void displayImageWithBoxes(BufferedImage bufImg, List<List<Float>> bboxes) {

	}

	//TODO
	public void displayImageWithBoxesAndLabels(BufferedImage bufImg, List<List<Float>> bboxes, List<String> labels) {

	}

	//TODO
	public void displayImageWithLabels(BufferedImage bufImg, List<String> labels) {

	}
}
