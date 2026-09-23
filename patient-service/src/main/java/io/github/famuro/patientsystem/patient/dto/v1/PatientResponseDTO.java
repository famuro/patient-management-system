package io.github.famuro.patientsystem.patient.dto.v1;

/**
 * Response model returned by the Patient REST API.
 *
 * <p>This record represents the patient data exposed to API consumers and
 * keeps the external response contract separate from the persistence model.
 * Values are produced by mapping the Patient domain entity into an API-safe
 * representation.
 *
 * @param id             unique identifier of the patient
 * @param name           patient's name
 * @param email          patient's email address
 * @param address        patient's address
 * @param dateOfBirth    patient's date of birth
 */
public record PatientResponseDTO(
        String id,
        String name,
        String email,
        String address,
        String dateOfBirth
) {}
