package io.github.famuro.patientsystem.patient.dto.v1;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request model for creating or updating a patient.
 *
 * <p>This record defines the data accepted at the Patient REST API boundary
 * and keeps external request payloads separate from the persistence entity.
 * Bean Validation constraints on the record components are applied before
 * requests reach the service layer.
 *
 * @param name        patient's name
 * @param email       patient's email address
 * @param address     patient's address
 * @param dateOfBirth patient's date of birth; future dates are not permitted
 */
public record PatientRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @NotNull(message = "Date of birth is required")
        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth
) {}
