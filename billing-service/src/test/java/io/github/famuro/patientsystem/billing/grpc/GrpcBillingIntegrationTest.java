package io.github.famuro.patientsystem.billing.grpc;

import io.github.famuro.patientsystem.contract.billing.v1.AccountStatus;
import io.github.famuro.patientsystem.contract.billing.v1.BillingRequest;
import io.github.famuro.patientsystem.contract.billing.v1.BillingResponse;
import io.github.famuro.patientsystem.contract.billing.v1.BillingServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestGrpcTransport
@ImportGrpcClients(types = BillingServiceGrpc.BillingServiceBlockingStub.class)
class GrpcBillingServiceIntegrationTest {

    @Autowired
    private BillingServiceGrpc.BillingServiceBlockingStub billingStub;

    @Test
    void createBillingAccountReturnsActiveAccount() {
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId("patient-123")
                .build();

        BillingResponse response = assertDoesNotThrow(
                () -> billingStub.createBillingAccount(request)
        );

        assertFalse(response.getAccountId().isBlank());
        assertEquals("patient-123", response.getPatientId());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
    }

    @Test
    void createBillingAccountReturnsInvalidArgumentWhenPatientIdIsBlank() {
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId("")
                .build();

        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> billingStub.createBillingAccount(request)
        );

        assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());

        assertEquals("patient_id is required", exception.getStatus().getDescription());
    }
}