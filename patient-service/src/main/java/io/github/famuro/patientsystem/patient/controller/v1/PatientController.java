package io.github.famuro.patientsystem.patient.controller.v1;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
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

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patient", description = "API for managing patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public List<PatientResponseDTO> getPatients() {
        return patientService.getPatients();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Patient By ID")
    public PatientResponseDTO getPatientById(@PathVariable UUID id) {
        return patientService.getPatientById(id);
    }

    @PostMapping
    @Operation(summary = "Create Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);

        URI location = URI.create("/api/v1/patients/" + patientResponseDTO.id());

        return ResponseEntity.created(location).body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Patient")
    public PatientResponseDTO updatePatient(@PathVariable UUID id,
                                            @Valid @RequestBody PatientRequestDTO updateRequestDTO) {

        return patientService.updatePatient(id, updateRequestDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Patient")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatientById(@PathVariable UUID id) {
        patientService.deletePatientById(id);
    }
}
