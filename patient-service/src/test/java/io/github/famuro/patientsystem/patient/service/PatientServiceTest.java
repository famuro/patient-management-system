package io.github.famuro.patientsystem.patient.service;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.exception.PatientNotFoundException;
import io.github.famuro.patientsystem.patient.mapper.PatientMapper;
import io.github.famuro.patientsystem.patient.model.Patient;
import io.github.famuro.patientsystem.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        PatientMapper patientMapper = new PatientMapper();
        patientService = new PatientService(patientRepository, patientMapper);
    }

    // =========================================================================
    // CREATE TESTS
    // =========================================================================
    @Test
    void createPatientSavesAndReturnsPatient() {
        PatientRequestDTO request = createPatientRequest();
        Patient savedPatient = mock(Patient.class);
        UUID id = UUID.randomUUID();

        when(patientRepository.existsByEmail(request.email())).thenReturn(false);

        when(savedPatient.getId()).thenReturn(id);
        when(savedPatient.getName()).thenReturn(request.name());
        when(savedPatient.getEmail()).thenReturn(request.email());
        when(savedPatient.getAddress()).thenReturn(request.address());
        when(savedPatient.getDateOfBirth()).thenReturn(request.dateOfBirth());

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponseDTO result = patientService.createPatient(request);

        assertEquals(id.toString(), result.id());
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
        assertEquals(request.address(), result.address());
        assertEquals(request.dateOfBirth().toString(), result.dateOfBirth());

        verify(patientRepository).existsByEmail(request.email());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatientThrowsWhenEmailAlreadyExists() {
        PatientRequestDTO request = createPatientRequest();

        when(patientRepository.existsByEmail(request.email())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> patientService.createPatient(request));

        assertEquals(
                "A patient with this email already exists",
                exception.getMessage()
        );

        verify(patientRepository).existsByEmail(request.email());
        verify(patientRepository, never()).save(any());
    }

    // =========================================================================
    // READ TESTS
    // =========================================================================
    @Test
    void getPatientsReturnsMappedPatients() {
        Patient patient = mock(Patient.class);
        UUID id = UUID.randomUUID();

        when(patient.getId()).thenReturn(id);
        when(patient.getName()).thenReturn("Jon Snow");
        when(patient.getEmail()).thenReturn("jon@example.com");
        when(patient.getAddress()).thenReturn("21 Jump St");
        when(patient.getDateOfBirth()).thenReturn(LocalDate.of(1990, Month.JANUARY, 1));

        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientResponseDTO> result = patientService.getPatients();

        assertEquals(1, result.size());

        PatientResponseDTO response = result.getFirst();
        assertEquals(id.toString(), response.id());
        assertEquals("Jon Snow", response.name());
        assertEquals("jon@example.com", response.email());
        assertEquals("21 Jump St", response.address());
        assertEquals("1990-01-01", response.dateOfBirth());

        verify(patientRepository).findAll();
    }

    @Test
    void getPatientsReturnsEmptyListWhenNoPatientsExist() {
        when(patientRepository.findAll()).thenReturn(List.of());

        List<PatientResponseDTO> result = patientService.getPatients();

        assertTrue(result.isEmpty());

        verify(patientRepository).findAll();
    }

    @Test
    void getPatientByIdReturnsPatientWhenFound() {
        Patient patient = mock(Patient.class);

        UUID id = UUID.randomUUID();

        when(patient.getId()).thenReturn(id);
        when(patient.getName()).thenReturn("Jon Snow");
        when(patient.getEmail()).thenReturn("jon@example.com");
        when(patient.getAddress()).thenReturn("21 Jump St");
        when(patient.getDateOfBirth()).thenReturn(LocalDate.of(1990, Month.JANUARY, 1));

        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

        PatientResponseDTO result = patientService.getPatientById(id);

        assertEquals(id.toString(), result.id());
        assertEquals("Jon Snow", result.name());
        assertEquals("jon@example.com", result.email());
        assertEquals("21 Jump St", result.address());
        assertEquals("1990-01-01", result.dateOfBirth());

        verify(patientRepository).findById(id);
    }

    @Test
    void getPatientByIdThrowsWhenPatientDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientService.getPatientById(id)
        );

        assertEquals("Patient not found with id " + id, exception.getMessage());

        verify(patientRepository).findById(id);
    }

    // =========================================================================
    // UPDATE TESTS
    // =========================================================================
    @Test
    void updatePatientReturnsUpdatedPatient() {
        UUID id = UUID.randomUUID();
        Patient patient = mock(Patient.class);

        PatientRequestDTO request = createPatientRequest();

        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
        when(patientRepository.existsByEmailAndIdNot(request.email(), id)).thenReturn(false);
        when(patientRepository.save(patient)).thenReturn(patient);

        when(patient.getId()).thenReturn(id);
        when(patient.getName()).thenReturn(request.name());
        when(patient.getEmail()).thenReturn(request.email());
        when(patient.getAddress()).thenReturn(request.address());
        when(patient.getDateOfBirth()).thenReturn(request.dateOfBirth());

        PatientResponseDTO result = patientService.updatePatient(id, request);

        assertEquals(id.toString(), result.id());
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
        assertEquals(request.address(), result.address());
        assertEquals(request.dateOfBirth().toString(), result.dateOfBirth());

        verify(patientRepository).findById(id);
        verify(patientRepository).existsByEmailAndIdNot(request.email(), id);

        verify(patient).setName(request.name());
        verify(patient).setEmail(request.email());
        verify(patient).setAddress(request.address());
        verify(patient).setDateOfBirth(request.dateOfBirth());

        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatientThrowsWhenPatientDoesNotExist() {
        UUID id = UUID.randomUUID();
        PatientRequestDTO request = createPatientRequest();

        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientService.updatePatient(id, request)
        );

        assertEquals(
                "Patient not found with id " + id,
                exception.getMessage()
        );

        verify(patientRepository).findById(id);
        verify(patientRepository, never()).existsByEmailAndIdNot(anyString(), any(UUID.class));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void updatePatientThrowsWhenEmailBelongsToAnotherPatient() {
        UUID id = UUID.randomUUID();
        Patient patient = mock(Patient.class);
        PatientRequestDTO request = createPatientRequest();

        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
        when(patientRepository.existsByEmailAndIdNot(request.email(), id)).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> patientService.updatePatient(id, request)
        );

        assertEquals(
                "A patient with this email already exists",
                exception.getMessage()
        );

        verify(patientRepository).findById(id);
        verify(patientRepository).existsByEmailAndIdNot(request.email(), id);
        verify(patientRepository, never()).save(any());
    }

    // =========================================================================
    // DELETE TESTS
    // =========================================================================
    @Test
    void deletePatientByIdDeletesPatientWhenFound() {
        UUID id = UUID.randomUUID();

        when(patientRepository.existsById(id)).thenReturn(true);

        patientService.deletePatientById(id);

        verify(patientRepository).existsById(id);
        verify(patientRepository).deleteById(id);
    }

    @Test
    void deletePatientByIdThrowsWhenPatientDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(patientRepository.existsById(id)).thenReturn(false);

        assertThrows(
                PatientNotFoundException.class,
                () -> patientService.deletePatientById(id)
        );

        verify(patientRepository).existsById(id);
        verify(patientRepository, never()).deleteById(id);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private PatientRequestDTO createPatientRequest() {
        return new PatientRequestDTO(
                "Jon Snow",
                "jon@example.com",
                "21 Jump St",
                LocalDate.of(1990, Month.JANUARY, 1)
        );
    }
}
