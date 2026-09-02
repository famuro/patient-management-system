package io.github.famuro.patientsystem.patient.controller;

import io.github.famuro.patientsystem.patient.dto.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.service.PatientService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private PatientService patientService;

    @Test
    void getPatientsReturnsPatients() throws Exception {
        PatientResponseDTO patient = createPatientResponse();

        when(patientService.getPatients())
                .thenReturn(List.of(patient));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(patient.getId()))
                .andExpect(jsonPath("$[0].name").value(patient.getName()))
                .andExpect(jsonPath("$[0].email").value(patient.getEmail()));

        verify(patientService).getPatients();
    }

    @Test
    void createPatientReturnsCreatedPatient() throws Exception {
        PatientRequestDTO request = createPatientRequest();
        PatientResponseDTO response = createPatientResponse();

        when(patientService.createPatient(any(PatientRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void createPatientReturnsBadRequestForInvalidRequest() throws Exception {
        PatientRequestDTO request = new PatientRequestDTO();

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"))
                .andExpect(jsonPath("$.email").value("Email is required"))
                .andExpect(jsonPath("$.address").value("Address is required"))
                .andExpect(jsonPath("$.dateOfBirth").value("Date of birth is required"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "test@",
            "@proton.me",
            "test example@yahoo.com",
            "@@google.com",
            "example@.com"
    })
    void createPatientReturnsBadRequestForInvalidEmail(String invalidEmail) throws Exception {
        PatientRequestDTO request = createPatientRequest();
        request.setEmail(invalidEmail);

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email address"));
    }

    @ParameterizedTest
    @MethodSource("futureDatesOfBirth")
    void createPatientReturnsBadRequestForFutureDateOfBirth(LocalDate dateOfBirth) throws Exception {
        PatientRequestDTO request = createPatientRequest();
        request.setDateOfBirth(dateOfBirth);

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dateOfBirth").value("Date of birth cannot be in the future"));
    }

    @Test
    void createPatientAcceptsTodayDateOfBirth() throws Exception {
        PatientRequestDTO request = createPatientRequest();
        request.setDateOfBirth(LocalDate.now(ZoneId.systemDefault()));

        PatientResponseDTO response = createPatientResponse();

        when(patientService.createPatient(any(PatientRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createPatientReturnsConflictWhenEmailAlreadyExists() throws Exception {
        PatientRequestDTO request = createPatientRequest();

        when(patientService.createPatient(any(PatientRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException(
                        "A patient with this email already exists"
                ));

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    private PatientRequestDTO createPatientRequest() {
        PatientRequestDTO request = new PatientRequestDTO();
        request.setName("Jon Snow");
        request.setEmail("jon@example.com");
        request.setAddress("21 Jump St");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        return request;
    }

    private PatientResponseDTO createPatientResponse() {
        PatientResponseDTO response = new PatientResponseDTO();
        response.setId(UUID.randomUUID().toString());
        response.setName("Jon Snow");
        response.setEmail("jon@example.com");
        response.setAddress("21 Jump St");
        response.setDateOfBirth("1990-01-01");

        return response;
    }

    private static Stream<LocalDate> futureDatesOfBirth() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        return Stream.of(
                today.plusDays(1),
                today.plusMonths(1),
                today.plusYears(10)
        );
    }
}
