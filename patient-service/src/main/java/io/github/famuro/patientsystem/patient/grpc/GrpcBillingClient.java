package io.github.famuro.patientsystem.patient.grpc;

import io.github.famuro.patientsystem.contract.billing.v1.BillingRequest;
import io.github.famuro.patientsystem.contract.billing.v1.BillingResponse;
import io.github.famuro.patientsystem.contract.billing.v1.BillingServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Client adapter for communicating with the Billing Service over gRPC.
 *
 * <p>Builds Billing protobuf requests and delegates synchronous RPC calls
 * through the generated {@link BillingServiceGrpc.BillingServiceBlockingStub}.
 */
@Service
public class GrpcBillingClient {
    private static final Logger log = LoggerFactory.getLogger(GrpcBillingClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public GrpcBillingClient(BillingServiceGrpc.BillingServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    /**
     * Creates a Billing account for a patient.
     *
     * @param id    patient identifier
     * @param name  patient name
     * @param email patient email address
     */
    public void createBillingAccount(String id, String name, String email) {
        log.debug("Connecting to the gRPC Billing server at channel: {}", blockingStub.getChannel());
        log.debug("Sending Billing account creation request for patient id {}", id);

        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(id)
                .setName(name)
                .setEmail(email)
                .build();

        // The generated blocking stub performs a synchronous RPC. The current
        // request thread waits until Billing returns a response or the call fails.
        BillingResponse response = blockingStub.createBillingAccount(request);

        log.debug("Received response from the gRPC Billing server: {}", response);
    }
}
