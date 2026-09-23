package io.github.famuro.patientsystem.patient.controller.v1;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.exception.PatientNotFoundException;
import io.github.famuro.patientsystem.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Patient Service operations.
 *
 * <p>Exposes the versioned Patient API under {@code /api/v1/patients}
 * and delegates patient-related business operations to {@link PatientService}.
 * The controller handles HTTP requests, response status codes, and
 * REST-specific response metadata such as the {@code Location} header.
 */
@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patient", description = "API for managing patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Retrieves all patients.
     *
     * <p>An empty collection is returned when no patients exist.
     *
     * @return all currently available patients
     */
    @GetMapping
    @Operation(summary = "Get Patients")
    public List<PatientResponseDTO> getPatients() {
        return patientService.getPatients();
    }

    /**
     * Retrieves a patient by identifier.
     *
     * @param id unique identifier of the patient
     * @return the matching patient
     * @throws PatientNotFoundException if no patient exists with the supplied ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Patient By ID")
    public PatientResponseDTO getPatientById(@PathVariable UUID id) {
        return patientService.getPatientById(id);
    }

    /**
     * Creates a new patient.
     *
     * <p>The request body is validated before the creation workflow begins.
     * Successful creation returns {@code 201 Created} and includes a
     * {@code Location} header identifying the newly created patient resource.
     *
     * <p>Creating a patient also initiates creation of the corresponding
     * Billing account through the Patient Service's internal gRPC integration.
     *
     * @param request patient data supplied by the client
     * @return the newly created patient
     */
    @PostMapping
    @Operation(summary = "Create Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO request) {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(request);

        URI location = URI.create("/api/v1/patients/" + patientResponseDTO.id());

        return ResponseEntity.created(location).body(patientResponseDTO);
    }

    /**
     * Updates an existing patient.
     *
     * @param id      unique identifier of the patient
     * @param request updated patient data
     * @return the updated patient
     * @throws PatientNotFoundException if no patient exists with the supplied ID
     * @throws EmailAlreadyExistsException if the email belongs to another patient
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Patient")
    public PatientResponseDTO updatePatient(@PathVariable UUID id,
                                            @Valid @RequestBody PatientRequestDTO request) {

        return patientService.updatePatient(id, request);
    }

    /**
     * Deletes a patient by identifier.
     *
     * @param id unique identifier of the patient
     * @throws PatientNotFoundException if no patient exists with the supplied ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Patient")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatientById(@PathVariable UUID id) {
        patientService.deletePatientById(id);
    }
}
