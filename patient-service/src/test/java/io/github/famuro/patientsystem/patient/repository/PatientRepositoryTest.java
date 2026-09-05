package io.github.famuro.patientsystem.patient.repository;

import io.github.famuro.patientsystem.patient.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void existsByEmailReturnsTrueWhenPatientExists() {
        Patient patient = createPatient("jon@example.com");

        patientRepository.saveAndFlush(patient);

        assertTrue(patientRepository.existsByEmail("jon@example.com"));
    }

    @Test
    void existsByEmailReturnsFalseWhenPatientDoesNotExist() {
        assertFalse(patientRepository.existsByEmail("missing@example.com"));
    }

    private Patient createPatient(String email) {
        Patient patient = new Patient();
        patient.setName("Jon Snow");
        patient.setEmail(email);
        patient.setAddress("21 Jump St");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setRegisteredDate(LocalDate.of(2026, 9, 2));

        return patient;
    }
}