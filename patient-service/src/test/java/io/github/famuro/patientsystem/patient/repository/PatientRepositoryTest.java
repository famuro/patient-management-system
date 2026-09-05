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

    @Test
    void existsByEmailAndIdNotReturnsTrueWhenAnotherPatientHasEmail() {
        Patient firstPatient = createPatient("first@example.com");
        Patient secondPatient = createPatient("second@example.com");

        firstPatient = patientRepository.saveAndFlush(firstPatient);
        secondPatient = patientRepository.saveAndFlush(secondPatient);

        boolean exists = patientRepository.existsByEmailAndIdNot(
                secondPatient.getEmail(),
                firstPatient.getId()
        );

        assertTrue(exists);
    }

    @Test
    void existsByEmailAndIdNotReturnsFalseWhenEmailBelongsToSamePatient() {
        Patient patient = createPatient("patient@example.com");

        patient = patientRepository.saveAndFlush(patient);

        boolean exists = patientRepository.existsByEmailAndIdNot(
                patient.getEmail(),
                patient.getId()
        );

        assertFalse(exists);
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