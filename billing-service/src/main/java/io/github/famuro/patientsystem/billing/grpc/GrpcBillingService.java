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

@GrpcService
public class GrpcBillingService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(GrpcBillingService.class);

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver) {

        log.info("Create billing account request: {}", billingRequest);

        String patientId = billingRequest.getPatientId();

        if (patientId.isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("patient_id is required")
                            .asRuntimeException()
            );

            return;
        }

        String accountId = UUID.randomUUID().toString();

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId(accountId)
                .setPatientId(patientId)
                .setStatus(AccountStatus.ACTIVE)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
