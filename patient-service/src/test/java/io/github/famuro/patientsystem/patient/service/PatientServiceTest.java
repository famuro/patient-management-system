package io.github.famuro.patientsystem.patient.service;

import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.exception.PatientNotFoundException;
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
        patientService = new PatientService(patientRepository);
    }

    // =========================================================================
    // CREATE TESTS
    // =========================================================================
    @Test
    void createPatientSavesAndReturnsPatient() {
        PatientRequestDTO request = createPatientRequest();
        Patient savedPatient = mock(Patient.class);
        UUID id = UUID.randomUUID();

        when(patientRepository.existsByEmail(request.getEmail())).thenReturn(false);

        when(savedPatient.getId()).thenReturn(id);
        when(savedPatient.getName()).thenReturn(request.getName());
        when(savedPatient.getEmail()).thenReturn(request.getEmail());
        when(savedPatient.getAddress()).thenReturn(request.getAddress());
        when(savedPatient.getDateOfBirth()).thenReturn(request.getDateOfBirth());

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponseDTO result = patientService.createPatient(request);

        assertEquals(id.toString(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getAddress(), result.getAddress());

        verify(patientRepository).existsByEmail(request.getEmail());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatientThrowsWhenEmailAlreadyExists() {
        PatientRequestDTO request = createPatientRequest();

        when(patientRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> patientService.createPatient(request)
        );

        verify(patientRepository).existsByEmail(request.getEmail());
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
        assertEquals(id.toString(), response.getId());
        assertEquals("Jon Snow", response.getName());
        assertEquals("jon@example.com", response.getEmail());
        assertEquals("21 Jump St", response.getAddress());
        assertEquals("1990-01-01", response.getDateOfBirth());

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

        assertEquals(id.toString(), result.getId());
        assertEquals("Jon Snow", result.getName());
        assertEquals("jon@example.com", result.getEmail());
        assertEquals("21 Jump St", result.getAddress());
        assertEquals("1990-01-01", result.getDateOfBirth());

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
        when(patientRepository.existsByEmailAndIdNot(request.getEmail(), id)).thenReturn(false);
        when(patientRepository.save(patient)).thenReturn(patient);

        when(patient.getId()).thenReturn(id);
        when(patient.getName()).thenReturn(request.getName());
        when(patient.getEmail()).thenReturn(request.getEmail());
        when(patient.getAddress()).thenReturn(request.getAddress());
        when(patient.getDateOfBirth()).thenReturn(request.getDateOfBirth());

        PatientResponseDTO result = patientService.updatePatient(id, request);

        assertEquals(id.toString(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getAddress(), result.getAddress());
        assertEquals(
                request.getDateOfBirth().toString(),
                result.getDateOfBirth()
        );

        verify(patientRepository).findById(id);
        verify(patientRepository).existsByEmailAndIdNot(request.getEmail(), id);

        verify(patient).setName(request.getName());
        verify(patient).setEmail(request.getEmail());
        verify(patient).setAddress(request.getAddress());
        verify(patient).setDateOfBirth(request.getDateOfBirth());

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
        when(patientRepository.existsByEmailAndIdNot(request.getEmail(), id)).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> patientService.updatePatient(id, request)
        );

        assertEquals(
                "A patient with this email already exists",
                exception.getMessage()
        );

        verify(patientRepository).findById(id);
        verify(patientRepository).existsByEmailAndIdNot(request.getEmail(), id);
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
        PatientRequestDTO request = new PatientRequestDTO();
        request.setName("Jon Snow");
        request.setEmail("jon@example.com");
        request.setAddress("21 Jump St");
        request.setDateOfBirth(LocalDate.of(1990, Month.JANUARY, 1));

        return request;
    }
}
