package io.github.famuro.patientsystem.patient.controller;

import io.github.famuro.patientsystem.patient.controller.v1.PatientController;
import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.dto.v1.PatientResponseDTO;
import io.github.famuro.patientsystem.patient.error.ErrorMessages;
import io.github.famuro.patientsystem.patient.exception.EmailAlreadyExistsException;
import io.github.famuro.patientsystem.patient.exception.PatientNotFoundException;
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
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private PatientService patientService;

    // =========================================================================
    // GET TESTS
    // =========================================================================
    @Test
    void getPatientsReturnsPatients() throws Exception {
        PatientResponseDTO patient = createPatientResponse(UUID.randomUUID());
        PatientResponseDTO patient2 = createPatientResponse(UUID.randomUUID());

        when(patientService.getPatients()).thenReturn(List.of(patient, patient2));

        mockMvc.perform(get(PATIENTS_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(patient.id()))
                .andExpect(jsonPath("$[0].name").value(patient.name()))
                .andExpect(jsonPath("$[0].email").value(patient.email()))
                .andExpect(jsonPath("$[0].address").value(patient.address()))
                .andExpect(jsonPath("$[0].dateOfBirth").value(patient.dateOfBirth()))
                .andExpect(jsonPath("$[1].id").value(patient2.id()))
                .andExpect(jsonPath("$[1].name").value(patient2.name()))
                .andExpect(jsonPath("$[1].email").value(patient2.email()))
                .andExpect(jsonPath("$[1].address").value(patient2.address()))
                .andExpect(jsonPath("$[1].dateOfBirth").value(patient2.dateOfBirth()));

        verify(patientService).getPatients();
    }

    @Test
    void getPatientsReturnsEmptyListWhenNoPatientsExist() throws Exception {
        when(patientService.getPatients()).thenReturn(List.of());

        mockMvc.perform(get(PATIENTS_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(patientService).getPatients();
    }

    @Test
    void getPatientByIdReturnsPatient() throws Exception {
        UUID id = UUID.randomUUID();

        PatientResponseDTO response = createPatientResponse(id);

        when(patientService.getPatientById(id)).thenReturn(response);

        mockMvc.perform(get(PATIENTS_API_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.address").value(response.address()))
                .andExpect(jsonPath("$.dateOfBirth").value(response.dateOfBirth()));

        verify(patientService).getPatientById(id);
    }

    @Test
    void getPatientByIdReturnsNotFoundWhenPatientDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(patientService.getPatientById(id))
                .thenThrow(new PatientNotFoundException("Patient not found with id " + id));

        mockMvc.perform(get(PATIENTS_API_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.PATIENT_NOT_FOUND_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("Patient not found with id " + id))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/" + id));

        verify(patientService).getPatientById(id);
    }

    @Test
    void getPatientByIdReturnsBadRequestForInvalidUuid() throws Exception {
        mockMvc.perform(get(PATIENTS_API_URL + "/{id}", "not-a-valid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.INVALID_PARAMETER_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid value for parameter 'id'"))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/not-a-valid-uuid"));

        verifyNoInteractions(patientService);
    }

    // =========================================================================
    // POST TESTS
    // =========================================================================
    @Test
    void createPatientReturnsCreatedPatient() throws Exception {
        UUID id = UUID.randomUUID();

        PatientRequestDTO request = createPatientRequest();
        PatientResponseDTO response = createPatientResponseFromRequest(id, request);

        when(patientService.createPatient(request)).thenReturn(response);

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        PATIENTS_API_URL + "/" + id
                ))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.address").value(response.address()))
                .andExpect(jsonPath("$.dateOfBirth").value(response.dateOfBirth()));

        verify(patientService).createPatient(request);
    }

    @Test
    void createPatientReturnsBadRequestForMissingFields() throws Exception {
        PatientRequestDTO request = new PatientRequestDTO("", "", "", null);

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.VALIDATION_FAILED_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value(ErrorMessages.VALIDATION_FAILED_MESSAGE))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.address").exists())
                .andExpect(jsonPath("$.errors.dateOfBirth").exists());

        verifyNoInteractions(patientService);
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
        PatientRequestDTO request = createPatientRequestWithEmail(invalidEmail);

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.VALIDATION_FAILED_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value(ErrorMessages.VALIDATION_FAILED_MESSAGE))
                .andExpect(jsonPath("$.errors.email")
                        .value("Invalid email address"));

        verifyNoInteractions(patientService);
    }

    @ParameterizedTest
    @MethodSource("getMalformedJson")
    void createPatientReturnsBadRequestForMalformedJson(String malformedJson) throws Exception {
        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.INVALID_REQUEST_BODY_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value(ErrorMessages.INVALID_REQUEST_BODY_MESSAGE))
                .andExpect(jsonPath("$.instance").value(PATIENTS_API_URL));

        verifyNoInteractions(patientService);
    }

    @Test
    void createPatientReturnsBadRequestForUnknownField() throws Exception {
        String request = """
            {
              "name": "Anakin Skywalker",
              "email": "anakin@jedi.com",
              "address": "67 Mustafar Rd",
              "dateOfBirth": "1980-09-18",
              "unexpectedField": "value"
            }
            """;

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.INVALID_REQUEST_BODY_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value(ErrorMessages.INVALID_REQUEST_BODY_MESSAGE))
                .andExpect(jsonPath("$.instance").value(PATIENTS_API_URL));

        verifyNoInteractions(patientService);
    }

    @Test
    void createPatientRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("patient"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.UNSUPPORTED_MEDIA_TYPE_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value(ErrorMessages.UNSUPPORTED_MEDIA_TYPE_MESSAGE))
                .andExpect(jsonPath("$.instance").value(PATIENTS_API_URL));

        verifyNoInteractions(patientService);
    }

    @ParameterizedTest
    @MethodSource("getFutureDates")
    void createPatientReturnsBadRequestForFutureDateOfBirth(LocalDate dateOfBirth) throws Exception {
        PatientRequestDTO request = createPatientRequestWithDateOfBirth(dateOfBirth);

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value(ErrorMessages.VALIDATION_FAILED_TITLE))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.VALIDATION_FAILED_MESSAGE))
                .andExpect(jsonPath("$.instance").value(PATIENTS_API_URL))
                .andExpect(jsonPath("$.errors.dateOfBirth")
                        .value("Date of birth cannot be in the future"));

        verifyNoInteractions(patientService);
    }

    @Test
    void createPatientAcceptsTodayDateOfBirth() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        PatientRequestDTO request = createPatientRequestWithDateOfBirth(today);
        PatientResponseDTO response = createPatientResponseFromRequest(id, request);

        when(patientService.createPatient(request)).thenReturn(response);

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        PATIENTS_API_URL + "/" + id
                ))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.dateOfBirth").value(today.toString()));

        verify(patientService).createPatient(request);
    }

    @Test
    void createPatientReturnsConflictWhenEmailAlreadyExists() throws Exception {
        PatientRequestDTO request = createPatientRequest();

        when(patientService.createPatient(request))
                .thenThrow(new EmailAlreadyExistsException("A patient with this email already exists"));

        mockMvc.perform(post(PATIENTS_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.EMAIL_ALREADY_EXISTS_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("A patient with this email already exists"))
                .andExpect(jsonPath("$.instance").value(PATIENTS_API_URL));

        verify(patientService).createPatient(request);
    }

    // =========================================================================
    // PUT TESTS
    // =========================================================================
    @Test
    void updatePatientReturnsUpdatedPatient() throws Exception {
        UUID id = UUID.randomUUID();

        PatientRequestDTO request = createPatientRequest();

        PatientResponseDTO response = new PatientResponseDTO(
                id.toString(),
                request.name(),
                request.email(),
                request.address(),
                request.dateOfBirth().toString()
        );

        when(patientService.updatePatient(id, request)).thenReturn(response);

        mockMvc.perform(put(PATIENTS_API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.address").value(request.address()))
                .andExpect(jsonPath("$.dateOfBirth")
                        .value(request.dateOfBirth().toString()));

        verify(patientService).updatePatient(id, request);
    }

    @Test
    void updatePatientReturnsBadRequestForInvalidRequest() throws Exception {
        UUID id = UUID.randomUUID();

        PatientRequestDTO request = new PatientRequestDTO(
                "",
                "invalid-email",
                "",
                null
        );

        mockMvc.perform(put(PATIENTS_API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.VALIDATION_FAILED_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value(ErrorMessages.VALIDATION_FAILED_MESSAGE))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.address").exists())
                .andExpect(jsonPath("$.errors.dateOfBirth").exists());

        verifyNoInteractions(patientService);
    }

    @Test
    void updatePatientReturnsNotFoundWhenPatientDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        PatientRequestDTO request = createPatientRequest();

        when(patientService.updatePatient(id, request))
                .thenThrow(new PatientNotFoundException("Patient not found with id " + id));

        mockMvc.perform(put(PATIENTS_API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.PATIENT_NOT_FOUND_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("Patient not found with id " + id))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/" + id));

        verify(patientService).updatePatient(id, request);
    }

    @Test
    void updatePatientReturnsConflictWhenEmailAlreadyExists() throws Exception {
        UUID id = UUID.randomUUID();

        PatientRequestDTO request = createPatientRequest();

        when(patientService.updatePatient(id, request))
                .thenThrow(new EmailAlreadyExistsException("A patient with this email already exists"));

        mockMvc.perform(put(PATIENTS_API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.EMAIL_ALREADY_EXISTS_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("A patient with this email already exists"))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/" + id));

        verify(patientService).updatePatient(id, request);
    }

    @Test
    void updatePatientReturnsBadRequestForInvalidUuid() throws Exception {
        PatientRequestDTO request = createPatientRequest();

        mockMvc.perform(put(PATIENTS_API_URL + "/{id}", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.INVALID_PARAMETER_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid value for parameter 'id'"))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/not-a-uuid"));

        verifyNoInteractions(patientService);
    }

    // =========================================================================
    // DELETE TESTS
    // =========================================================================
    @Test
    void deletePatientByIdReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(patientService).deletePatientById(id);

        mockMvc.perform(delete(PATIENTS_API_URL + "/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(patientService).deletePatientById(id);
    }

    @Test
    void deletePatientByIdReturnsNotFoundWhenPatientDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new PatientNotFoundException("Patient not found with id " + id))
                .when(patientService).deletePatientById(id);

        mockMvc.perform(delete(PATIENTS_API_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.PATIENT_NOT_FOUND_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("Patient not found with id " + id))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/" + id));

        verify(patientService).deletePatientById(id);
    }

    @Test
    void deletePatientByIdReturnsBadRequestForInvalidUuid() throws Exception {

        mockMvc.perform(delete(PATIENTS_API_URL + "/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title")
                        .value(ErrorMessages.INVALID_PARAMETER_TITLE))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid value for parameter 'id'"))
                .andExpect(jsonPath("$.instance")
                        .value(PATIENTS_API_URL + "/not-a-uuid"));

        verifyNoInteractions(patientService);
    }

    // =========================================================================
    // HELPER METHODS & CONSTANTS
    // =========================================================================

    private static final String PATIENTS_API_URL = "/api/v1/patients";

    private PatientRequestDTO createPatientRequest() {
        return new PatientRequestDTO(
                "Jon Snow",
                "jon@knownothing.com",
                "21 Jump St",
                LocalDate.of(1990, Month.JANUARY, 1)
        );
    }

    private PatientRequestDTO createPatientRequestWithEmail(String email) {
        return new PatientRequestDTO(
                "Katniss Everdeen",
                email,
                "District 12 Blvd",
                LocalDate.of(1990, Month.JANUARY, 1)
        );
    }

    private PatientRequestDTO createPatientRequestWithDateOfBirth(LocalDate dateOfBirth) {
        return new PatientRequestDTO(
                "Avatar Aang",
                "aang@avatar.com",
                "Air Temple Rd",
                dateOfBirth
        );
    }

    private PatientResponseDTO createPatientResponse(UUID id) {
        return new PatientResponseDTO(
                id.toString(),
                "Spongebob Squarepants",
                "spongebob@krustykrab.com",
                "124 Conch Street, Bikini Bottom, Pacific Ocean",
                "1986-07-14"
        );
    }

    private PatientResponseDTO createPatientResponseFromRequest(UUID id, PatientRequestDTO request) {
        return new PatientResponseDTO(
                id.toString(),
                request.name(),
                request.email(),
                request.address(),
                request.dateOfBirth().toString()
        );
    }

    private static Stream<LocalDate> getFutureDates() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        return Stream.of(
                today.plusDays(1),
                today.plusMonths(1),
                today.plusYears(10)
        );
    }

    private static Stream<String> getMalformedJson() {
        return Stream.of(
                "{ Dirty Diana }",
                """
                {
                  "name": "Michael Jackson",
                  "email": "michael@kingofpop.com",
                  "address": "1 Thriller St",
                  "dateOfBirth": "1958-08-29"
                """,
                """
                {
                  "name": "Billie Jean"
                  "email": "billie@notmylover.com"
                  "address": "123 Rainbow Rd"
                  "dateOfBirth": "1960-05-19"
                }
                """
        );
    }
}
