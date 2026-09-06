package io.github.famuro.patientsystem.patient.dto.v1;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

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
