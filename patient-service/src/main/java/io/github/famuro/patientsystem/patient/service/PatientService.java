package io.github.famuro.patientsystem.patient.service;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.exception.PatientNotFoundException;
import io.github.famuro.patientsystem.patient.grpc.GrpcBillingClient;
import io.github.famuro.patientsystem.patient.mapper.PatientMapper;
import io.github.famuro.patientsystem.patient.model.Patient;
import io.github.famuro.patientsystem.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for patient lifecycle operations.
 *
 * <p>Handles patient creation, retrieval, updates, deletion, and coordination
 * with the Billing Service during patient creation.
 */
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final GrpcBillingClient grpcBillingClient;

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    public PatientService(PatientRepository patientRepository,
                          PatientMapper patientMapper,
                          GrpcBillingClient grpcBillingClient) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.grpcBillingClient = grpcBillingClient;
    }

    /**
     * Retrieves all patients.
     *
     * @return all patients as API response models
     */
    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(patientMapper::toDTO).toList();
    }

    /**
     * Retrieves a patient by identifier.
     *
     * @param id patient identifier
     * @return the matching patient
     * @throws PatientNotFoundException if the patient does not exist
     */
    public PatientResponseDTO getPatientById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id " + id));

        return patientMapper.toDTO(patient);
    }

    /**
     * Creates a patient and requests creation of the corresponding Billing account.
     *
     * @param request patient data to create
     * @return the created patient
     * @throws EmailAlreadyExistsException if the email is already in use
     */
    public PatientResponseDTO createPatient(PatientRequestDTO request) {
        // If an email exists, do not create a new patient
        if (patientRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists");
        }

        Patient patientDetails = patientMapper.toModel(request);
        Patient newPatient = patientRepository.save(patientDetails);

        log.info("Created patient with id {}", newPatient.getId());

        grpcBillingClient.createBillingAccount(
                newPatient.getId().toString(),
                newPatient.getName(),
                newPatient.getEmail()
        );

        log.info("Billing account creation completed for patient id {}", newPatient.getId());

        return patientMapper.toDTO(newPatient);
    }

    /**
     * Updates an existing patient.
     *
     * @param id      patient identifier
     * @param request updated patient data
     * @return the updated patient
     * @throws PatientNotFoundException if the patient does not exist
     * @throws EmailAlreadyExistsException if the email is already used by
     *         another patient
     */
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id " + id));

        // If a different patient already has the email address, do not update this patient's email to it
        if (patientRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists");
        }

        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setDateOfBirth(request.dateOfBirth());

        Patient updatedPatient = patientRepository.save(patient);

        log.info("Updated patient with id {}", updatedPatient.getId());

        return patientMapper.toDTO(updatedPatient);
    }

    /**
     * Deletes a patient by identifier.
     *
     * @param id patient identifier
     * @throws PatientNotFoundException if the patient does not exist
     */
    public void deletePatientById(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Patient not found with id " + id);
        }

        patientRepository.deleteById(id);

        log.info("Deleted patient with id {}", id);
    }
}
