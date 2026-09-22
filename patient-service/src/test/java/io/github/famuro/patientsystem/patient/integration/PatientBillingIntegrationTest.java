package io.github.famuro.patientsystem.patient.integration;

import io.github.famuro.patientsystem.contract.billing.v1.AccountStatus;
import io.github.famuro.patientsystem.contract.billing.v1.BillingRequest;
import io.github.famuro.patientsystem.contract.billing.v1.BillingResponse;
import io.github.famuro.patientsystem.contract.billing.v1.BillingServiceGrpc;
import io.github.famuro.patientsystem.patient.dto.v1.PatientRequestDTO;
import io.github.famuro.patientsystem.patient.grpc.GrpcBillingClient;
import io.github.famuro.patientsystem.patient.model.Patient;
import io.github.famuro.patientsystem.patient.repository.PatientRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Patient Service to Billing Service gRPC interaction.
 *
 * <p>This test verifies the complete Patient-side request flow from the REST API
 * through {@link GrpcBillingClient} and the generated gRPC client stub. A
 * test-only Billing gRPC implementation is registered with Spring and served
 * through the in-process gRPC test transport, allowing the gRPC boundary to be
 * exercised without starting the real Billing Service or opening a network port.
 *
 * <p>The Patient repository is mocked intentionally because database persistence
 * is outside the scope of this test. The purpose of this test is to verify that
 * successfully creating a patient results in the expected Billing gRPC request
 * being sent with the correct patient data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestGrpcTransport
@Import(PatientBillingIntegrationTest.GrpcTestConfiguration.class)
class PatientBillingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private TestGrpcBillingService testBillingGrpcService;

    @MockitoBean
    private PatientRepository patientRepository;

    /**
     * Verifies that creating a patient through the REST API sends the expected
     * Billing request to the Billing Service over gRPC.
     *
     * <p>This test exercises the Patient-side integration path from the REST
     * controller through PatientService, GrpcBillingClient, the generated gRPC
     * stub, and the in-process test gRPC server.
     */
    @Test
    void createPatientCreatesBillingAccount() throws Exception {
        UUID patientId = UUID.randomUUID();

        Patient savedPatient = mock(Patient.class);

        when(savedPatient.getId()).thenReturn(patientId);
        when(savedPatient.getName()).thenReturn("John Doe");
        when(savedPatient.getEmail()).thenReturn("john@example.com");
        when(savedPatient.getAddress()).thenReturn("123 Main St");
        when(savedPatient.getDateOfBirth()).thenReturn(LocalDate.of(1990, 1, 1));

        when(patientRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientRequestDTO request = new PatientRequestDTO(
                "John Doe",
                "john@example.com",
                "123 Main St",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        BillingRequest billingRequest = testBillingGrpcService.getLastRequest();

        assertNotNull(billingRequest);
        assertEquals(patientId.toString(), billingRequest.getPatientId());
        assertEquals(request.name(), billingRequest.getName());
        assertEquals(request.email(), billingRequest.getEmail());
    }

    /**
     * Provides gRPC server components used only by this integration test.
     *
     * <p>Registering {@link TestGrpcBillingService} as a Spring bean allows the
     * in-process gRPC test server to expose the Billing Service contract to the
     * Patient Service's real gRPC client.
     */
    @TestConfiguration
    static class GrpcTestConfiguration {

        @Bean
        TestGrpcBillingService testBillingGrpcService() {
            return new TestGrpcBillingService();
        }
    }

    /**
     * Test implementation of the Billing Service gRPC contract.
     *
     * <p>This service acts as the Billing Service during Patient integration tests.
     * It captures the most recent {@link BillingRequest} so the test can verify
     * the data transmitted across the gRPC boundary, then returns a successful
     * billing account response.
     *
     * <p>Using a real gRPC service implementation instead of mocking
     * {@link GrpcBillingClient} ensures that the generated client stub, protobuf
     * messages, and gRPC transport are exercised by the test.
     */
    static class TestGrpcBillingService extends BillingServiceGrpc.BillingServiceImplBase {

        private BillingRequest lastRequest;

        @Override
        public void createBillingAccount(
                BillingRequest request,
                StreamObserver<BillingResponse> responseObserver
        ) {
            this.lastRequest = request;

            BillingResponse response = BillingResponse.newBuilder()
                    .setPatientId(request.getPatientId())
                    .setAccountId(UUID.randomUUID().toString())
                    .setStatus(AccountStatus.ACTIVE)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        BillingRequest getLastRequest() {
            return lastRequest;
        }
    }
}