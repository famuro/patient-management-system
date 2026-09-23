package io.github.famuro.patientsystem.patient.exception;

import io.github.famuro.patientsystem.patient.error.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global REST exception handler for the Patient Service.
 *
 * <p>Converts application and request-processing exceptions into standardized
 * {@link ProblemDetail} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles requests for patients that do not exist.
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handlePatientNotFound(PatientNotFoundException exception, HttpServletRequest request) {
        log.warn(
                "Patient request failed because the patient was not found: {}",
                exception.getMessage()
        );

        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                ErrorMessages.PATIENT_NOT_FOUND_TITLE,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles email conflicts during patient creation or update.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception, HttpServletRequest request) {
        log.warn("Patient request rejected due to duplicate email");

        return createProblemDetail(
                HttpStatus.CONFLICT,
                ErrorMessages.EMAIL_ALREADY_EXISTS_TITLE,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles Bean Validation failures for request payloads.
     *
     * <p>Validation errors are returned by field in the {@code errors} property
     * of the response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.debug(
                "Patient request validation failed for {} field(s)",
                exception.getBindingResult().getFieldErrorCount()
        );

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.VALIDATION_FAILED_TITLE,
                ErrorMessages.VALIDATION_FAILED_MESSAGE,
                request
        );

        problemDetail.setProperty("errors", fieldErrors);

        return problemDetail;
    }

    /**
     * Handles request bodies that cannot be read or deserialized.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn("Patient request body could not be read or deserialized");

        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.INVALID_REQUEST_BODY_TITLE,
                ErrorMessages.INVALID_REQUEST_BODY_MESSAGE,
                request
        );
    }

    /**
     * Handles requests that use an unsupported media type.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Patient request rejected due to unsupported media type: {}",
                exception.getContentType()
        );

        return createProblemDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorMessages.UNSUPPORTED_MEDIA_TYPE_TITLE,
                ErrorMessages.UNSUPPORTED_MEDIA_TYPE_MESSAGE,
                request
        );
    }

    /**
     * Handles request parameters that cannot be converted to the required type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid value supplied for parameter '{}'",
                exception.getName()
        );

        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.INVALID_PARAMETER_TITLE,
                "Invalid value for parameter '" + exception.getName() + "'",
                request
        );
    }

    // Creates the common ProblemDetail structure used by REST error responses.
    private ProblemDetail createProblemDetail(HttpStatus status,
                                              String title,
                                              String detail,
                                              HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }
}
