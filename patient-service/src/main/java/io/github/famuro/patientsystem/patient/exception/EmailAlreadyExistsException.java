package io.github.famuro.patientsystem.patient.exception;

/**
 * Exception thrown when a patient operation would use an email address
 * that is already associated with another patient.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
