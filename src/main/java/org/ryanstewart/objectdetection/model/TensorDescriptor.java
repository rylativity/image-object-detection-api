package org.ryanstewart.objectdetection.model;

import lombok.Getter;

public class TensorDescriptor {
	@Getter
	private int ndim;
	@Getter
	private String label;
	@Getter
	private String type;
	@Getter
	String configType;

	public TensorDescriptor(int ndim, String label, String type, String configType) throws Exception {
		if (ndim > 0 && ndim < 4) {
			this.ndim = ndim;
		} else {
			throw new Exception("Cannot create Tensor Object with " + ndim + "dimensions");
		}

		this.label = label;
		this.configType = configType;

		if (type.toLowerCase().equals("integer") || type.toLowerCase().equals("float")) {
			this.type = type.toLowerCase();
		} else {
			throw new Exception("Tensor type " + type
					+ " is invalid. All input and output tensors must be of type 'integer' or 'float'");
		}
	}
}
