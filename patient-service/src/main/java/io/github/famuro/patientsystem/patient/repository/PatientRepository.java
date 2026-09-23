package io.github.famuro.patientsystem.patient.repository;


import io.github.famuro.patientsystem.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Patient persistence and patient-specific lookup operations.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    /**
     * Checks whether a patient exists with the supplied email address.
     *
     * @param email email address to check
     * @return {@code true} if the email is already associated with a patient
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether an email address is associated with a patient other than
     * the specified patient.
     *
     * @param email email address to check
     * @param id    patient identifier to exclude
     * @return {@code true} if another patient uses the supplied email
     */
    boolean existsByEmailAndIdNot(String email, UUID id);
}
