package io.github.famuro.patientsystem.patient.dto.v1;

public record PatientResponseDTO(
        String id,
        String name,
        String email,
        String address,
        String dateOfBirth
) {}
