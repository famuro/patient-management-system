package io.github.famuro.patientsystem.patient.error;

public final class ErrorMessages {

    private ErrorMessages() {}

    // Error response titles
    public static final String PATIENT_NOT_FOUND_TITLE = "Patient Not Found";
    public static final String EMAIL_ALREADY_EXISTS_TITLE = "Email Already Exists";
    public static final String VALIDATION_FAILED_TITLE = "Validation Failed";
    public static final String INVALID_REQUEST_BODY_TITLE = "Invalid Request Body";
    public static final String INVALID_PARAMETER_TITLE = "Invalid Parameter";
    public static final String UNSUPPORTED_MEDIA_TYPE_TITLE = "Unsupported Media Type";

    // Error response messages
    public static final String VALIDATION_FAILED_MESSAGE = "Request validation failed";
    public static final String UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Content type is not supported";
    public static final String INVALID_REQUEST_BODY_MESSAGE = "Request body is malformed or contains invalid fields";
}
