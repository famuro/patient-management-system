package io.github.famuro.patientsystem.billing.grpc;

import io.github.famuro.patientsystem.contract.billing.v1.AccountStatus;
import io.github.famuro.patientsystem.contract.billing.v1.BillingRequest;
import io.github.famuro.patientsystem.contract.billing.v1.BillingResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GrpcBillingServiceTest {

    private GrpcBillingService grpcBillingService;

    @BeforeEach
    void setUp() {
        grpcBillingService = new GrpcBillingService();
    }

    @Test
    void createBillingAccountReturnsActiveAccount() {
        BillingRequest request = BillingRequest.newBuilder().setPatientId("patient-123").build();

        @SuppressWarnings("unchecked")
        StreamObserver<BillingResponse> responseObserver = mock(StreamObserver.class);

        ArgumentCaptor<BillingResponse> responseCaptor = ArgumentCaptor.forClass(BillingResponse.class);

        grpcBillingService.createBillingAccount(request, responseObserver);

        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        BillingResponse response = responseCaptor.getValue();

        assertFalse(response.getAccountId().isBlank());
        assertEquals("patient-123", response.getPatientId());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
    }

    @Test
    void createBillingAccountReturnsInvalidArgumentWhenPatientIdIsBlank() {
        BillingRequest request = BillingRequest.newBuilder().setPatientId("").build();

        @SuppressWarnings("unchecked")
        StreamObserver<BillingResponse> responseObserver = mock(StreamObserver.class);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        grpcBillingService.createBillingAccount(request, responseObserver);

        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = (StatusRuntimeException) errorCaptor.getValue();

        assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());

        assertEquals("patient_id is required", exception.getStatus().getDescription());
    }
}