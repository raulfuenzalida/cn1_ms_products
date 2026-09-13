package duoc.cn1.ms_products.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(FilamentNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleFilamentNotFound(FilamentNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, "FILAMENT_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(FilamentInactiveException.class)
	public ResponseEntity<ErrorResponse> handleFilamentInactive(FilamentInactiveException ex, WebRequest request) {
		return build(HttpStatus.CONFLICT, "FILAMENT_INACTIVE", ex.getMessage(), request);
	}

	@ExceptionHandler(ProductPriceOutdatedException.class)
	public ResponseEntity<ErrorResponse> handlePriceOutdated(ProductPriceOutdatedException ex, WebRequest request) {
		return build(HttpStatus.CONFLICT, "PRODUCT_PRICE_OUTDATED", ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidProductStatusException.class)
	public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidProductStatusException ex, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_STATUS", ex.getMessage(), request);
	}

	@ExceptionHandler(TagNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTagNotFound(TagNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(DuplicateTagException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateTag(DuplicateTagException ex, WebRequest request) {
		return build(HttpStatus.CONFLICT, "DUPLICATE_TAG", ex.getMessage(), request);
	}

	@ExceptionHandler(ImageNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleImageNotFound(ImageNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, "IMAGE_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(ConfigServiceUnavailableException.class)
	public ResponseEntity<ErrorResponse> handleConfigUnavailable(ConfigServiceUnavailableException ex, WebRequest request) {
		return build(HttpStatus.SERVICE_UNAVAILABLE, "CONFIG_SERVICE_UNAVAILABLE", ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			errors.put(fieldName, error.getDefaultMessage());
		});
		ErrorResponse body = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"VALIDATION_ERROR",
			"Error de validación en los campos enviados",
			path(request),
			errors
		);
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, "INVALID_OPERATION", ex.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Error interno del servidor", request);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			status.value(),
			status.getReasonPhrase(),
			code,
			message,
			path(request)
		);
		return new ResponseEntity<>(error, status);
	}

	private String path(WebRequest request) {
		return request.getDescription(false).replace("uri=", "");
	}
}
