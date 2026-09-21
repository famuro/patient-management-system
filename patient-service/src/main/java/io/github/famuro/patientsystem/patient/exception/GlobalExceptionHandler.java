package io.github.famuro.patientsystem.patient.exception;

import io.github.famuro.patientsystem.patient.error.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handlePatientNotFound(PatientNotFoundException exception, HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                ErrorMessages.PATIENT_NOT_FOUND_TITLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception, HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                ErrorMessages.EMAIL_ALREADY_EXISTS_TITLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.INVALID_REQUEST_BODY_TITLE,
                ErrorMessages.INVALID_REQUEST_BODY_MESSAGE,
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorMessages.UNSUPPORTED_MEDIA_TYPE_TITLE,
                ErrorMessages.UNSUPPORTED_MEDIA_TYPE_MESSAGE,
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.INVALID_PARAMETER_TITLE,
                "Invalid value for parameter '" + exception.getName() + "'",
                request
        );
    }

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
