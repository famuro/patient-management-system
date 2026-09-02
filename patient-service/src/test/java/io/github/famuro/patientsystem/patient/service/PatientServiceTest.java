package io.github.famuro.patientsystem.patient.service;

import io.github.famuro.patientsystem.patient.dto.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.model.Patient;
import io.github.famuro.patientsystem.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void getPatientsReturnsMappedPatients() {
        Patient patient = mock(Patient.class);
        UUID id = UUID.randomUUID();

        when(patient.getId()).thenReturn(id);
        when(patient.getName()).thenReturn("Jon Snow");
        when(patient.getEmail()).thenReturn("jon@example.com");
        when(patient.getAddress()).thenReturn("21 Jump St");
        when(patient.getDateOfBirth()).thenReturn(LocalDate.of(1990, 1, 1));

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

    private PatientRequestDTO createPatientRequest() {
        PatientRequestDTO request = new PatientRequestDTO();
        request.setName("Jon Snow");
        request.setEmail("jon@example.com");
        request.setAddress("21 Jump St");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        return request;
    }
}