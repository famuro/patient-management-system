package io.github.famuro.patientsystem.patient.mapper;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.model.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Maps between Patient domain entities and API DTOs.
 */
@Component
public class PatientMapper {

    /**
     * Converts a Patient entity into its API response representation.
     *
     * @param patient patient entity
     * @return mapped patient response
     */
    public PatientResponseDTO toDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId().toString(),
                patient.getName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getDateOfBirth().toString()
        );
    }

    /**
     * Converts a patient request into a Patient entity.
     *
     * @param request patient request data
     * @return mapped Patient entity
     */
    public Patient toModel(PatientRequestDTO request) {
        Patient patient = new Patient();

        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setRegisteredDate(LocalDate.now(ZoneId.systemDefault()));

        return patient;
    }
}
