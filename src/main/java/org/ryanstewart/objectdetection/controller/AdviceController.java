package org.ryanstewart.objectdetection.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.ryanstewart.objectdetection.constants.Printer;
import org.ryanstewart.objectdetection.exception.ApiError;
import org.ryanstewart.objectdetection.exception.BaseEntityNotFoundException;
import org.ryanstewart.objectdetection.exception.BaseException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class AdviceController extends ResponseEntityExceptionHandler {
	private static final String GENERIC_ERROR_TITLE = "An error has taken place";

	private static final String GENERIC_ERROR_BODY = "An error has taken place. Please try your request again. If you continue to see this error, contact your Systems Administrator.";

	private static final String[] ERROR_WHITELIST = {
			"first name", "last name", "social security number", "title", "office name"
	};

	public AdviceController() {
		super();
	}

	private ResponseEntity<Object> getObjectResponseEntity(
			final MethodArgumentNotValidException ex, final HttpHeaders headers, final WebRequest request) {
		return getObjectResponseEntity(ex, headers, request);
	}

	@ExceptionHandler({ BaseEntityNotFoundException.class })
	public ResponseEntity<Object> handleBaseEntityNotFoundException(final BaseEntityNotFoundException ex) {
		handleLog(ex);
		//
		final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), ex.getMessage());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@ExceptionHandler({ BaseException.class })
	public ResponseEntity<Object> handleBaseException(final BaseException ex) {
		handleLog(ex);
		//
		final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), ex.getMessage());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex) {
		handleLog(ex);
		//
		final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> whiteListRMFConstraintViolationsWhichAreReportableToEndUser(
			final ConstraintViolationException ex) {
		handleLog(ex);
		//
		Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
		Optional<ConstraintViolation<?>> constrain = violations.stream().findFirst();
		if (constrain.isPresent()) {
			String message = constrain.get().getMessage().toLowerCase();
			for (String listItem : ERROR_WHITELIST) {
				if (message.contains(listItem.toLowerCase())) {
					final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, GENERIC_ERROR_BODY, message);
					return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
				}
			}
		}
		//
		final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_BODY,
				"An error occurred.");
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 405
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
			final HttpRequestMethodNotSupportedException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		//
		final StringBuilder builder = new StringBuilder();
		builder.append(ex.getMethod());
		builder.append(" method is not supported for this request. Supported methods are ");
		ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));
		final ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage(),
				builder.toString());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 415
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
			final HttpMediaTypeNotSupportedException ex,
			final HttpHeaders headers,
			final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		//
		final StringBuilder builder = new StringBuilder();
		builder.append(ex.getContentType());
		builder.append(" media type is not supported. Supported media types are ");
		ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));
		final ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getLocalizedMessage(),
				builder.substring(0, builder.length() - 2));
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		//
		final String error = ex.getParameterName() + " parameter is missing";
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(
			final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		//
		final String error = ex.getValue() + " value for " + ex.getPropertyName() + " should be of type "
				+ ex.getRequiredType();
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		return getObjectResponseEntity(ex, headers, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart(
			final MissingServletRequestPartException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		//
		final String error = ex.getRequestPartName() + " part is missing";
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleBindException(
			final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		handleLog(ex);
		//
		final List<String> errors = new ArrayList<>();
		for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getField() + ": " + error.getDefaultMessage());
		}
		for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
		}
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
		return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
	}

	// 404
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
			final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		handleLog(ex);
		//
		final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
		final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 500
	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(
			final Exception ex, final WebRequest request) {
		handleLog(ex);
		//
		final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_TITLE,
				GENERIC_ERROR_TITLE);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	private void handleLog(final Exception ex) {
		LOG.error(Printer.DIVIDER);
		LOG.error("CLASSNAME: " + ex.getClass().getName());
		LOG.error(ex.getMessage(), ex);
		LOG.error(Printer.DIVIDER);
	}
}
