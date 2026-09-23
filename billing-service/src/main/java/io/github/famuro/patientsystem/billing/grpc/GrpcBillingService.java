package io.github.famuro.patientsystem.billing.grpc;

import io.github.famuro.patientsystem.contract.billing.v1.AccountStatus;
import io.github.famuro.patientsystem.contract.billing.v1.BillingRequest;
import io.github.famuro.patientsystem.contract.billing.v1.BillingResponse;
import io.github.famuro.patientsystem.contract.billing.v1.BillingServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * gRPC service implementation for Billing account operations.
 *
 * <p>Handles Billing RPC requests defined by the shared protobuf contract
 * and returns the corresponding Billing responses.
 */
@GrpcService
public class GrpcBillingService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(GrpcBillingService.class);

    /**
     * Creates a Billing account for a patient.
     *
     * <p><b>NOTE: </b>Requests without a patient identifier are rejected with
     * {@code INVALID_ARGUMENT}.
     *
     * @param billingRequest        Billing account creation request
     * @param responseObserver      observer used to return the gRPC response
     */
    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver) {

        String patientId = billingRequest.getPatientId();

        if (patientId.isBlank()) {
            log.warn("Billing account creation rejected because patient id is missing");

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("patient_id is required")
                            .asRuntimeException()
            );
            return;
        }

        log.info("Received a billing account creation request for patient id {}", patientId);

        String accountId = UUID.randomUUID().toString();

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId(accountId)
                .setPatientId(patientId)
                .setStatus(AccountStatus.ACTIVE)
                .build();

        log.info("Created Billing account {} for patient id {}", accountId, billingRequest.getPatientId());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
