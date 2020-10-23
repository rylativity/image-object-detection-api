package org.ryanstewart.objectdetection.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApiError {
	@SuppressWarnings("unused")
	private HttpStatus status;

	@SuppressWarnings("unused")
	private String message;

	@SuppressWarnings("unused")
	private List<String> errors;

	public ApiError(
			final HttpStatus status,
			final String message,
			final List<String> errors) {
		super();
		this.status = status;
		this.message = message;
		this.errors = errors;
	}

	public ApiError(
			final HttpStatus status,
			final String message,
			final String error) {
		super();
		this.status = status;
		this.message = message;
		this.errors = Arrays.asList(error);
	}
}
