package io.github.famuro.patientsystem.patient.error;

/**
 * Centralized user-facing error titles and messages used by the Patient Service
 * REST error-handling layer.
 *
 * <p>This maintains a consistent error contract across standardized
 * {@link org.springframework.http.ProblemDetail} responses.
 */
public final class ErrorMessages {

    private ErrorMessages() {}

    // Resource and business-rule errors
    public static final String PATIENT_NOT_FOUND_TITLE = "Patient Not Found";
    public static final String EMAIL_ALREADY_EXISTS_TITLE = "Email Already Exists";

    // Request validation errors
    public static final String VALIDATION_FAILED_TITLE = "Validation Failed";
    public static final String VALIDATION_FAILED_MESSAGE = "Request validation failed";
    public static final String INVALID_REQUEST_BODY_TITLE = "Invalid Request Body";
    public static final String INVALID_REQUEST_BODY_MESSAGE = "Request body is malformed or contains invalid fields";
    public static final String INVALID_PARAMETER_TITLE = "Invalid Parameter";

    // HTTP content negotiation errors
    public static final String UNSUPPORTED_MEDIA_TYPE_TITLE = "Unsupported Media Type";
    public static final String UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Content type is not supported";
}
