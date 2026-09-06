package io.github.famuro.patientsystem.patient.mapper;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.model.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class PatientMapper {

    public PatientResponseDTO toDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId().toString(),
                patient.getName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getDateOfBirth().toString()
        );
    }

    public Patient toModel(PatientRequestDTO patientRequestDTO) {
        Patient patient = new Patient();

        patient.setName(patientRequestDTO.name());
        patient.setEmail(patientRequestDTO.email());
        patient.setAddress(patientRequestDTO.address());
        patient.setDateOfBirth(patientRequestDTO.dateOfBirth());
        patient.setRegisteredDate(LocalDate.now(ZoneId.systemDefault()));

        return patient;
    }
}
