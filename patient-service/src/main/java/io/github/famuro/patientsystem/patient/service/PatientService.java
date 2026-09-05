package io.github.famuro.patientsystem.patient.service;

import io.github.famuro.patientsystem.patient.dto.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.mapper.PatientMapper;
import io.github.famuro.patientsystem.patient.model.Patient;
import io.github.famuro.patientsystem.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        // If an email exists, do not create a new patient
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists");
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        return PatientMapper.toDTO(newPatient);
    }
}
