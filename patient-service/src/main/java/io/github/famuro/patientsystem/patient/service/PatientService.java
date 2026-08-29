package io.github.famuro.patientsystem.patient.service;

import io.github.famuro.patientsystem.patient.dto.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.mapper.PatientMapper;
import io.github.famuro.patientsystem.patient.model.Patient;
import io.github.famuro.patientsystem.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatientService {
    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients
                .stream()
                .map(PatientMapper::toDTO)
                .toList();
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }
}
