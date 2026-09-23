package io.github.famuro.patientsystem.patient.exception;

/**
 * Exception thrown when a requested patient cannot be found.
 */
public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
