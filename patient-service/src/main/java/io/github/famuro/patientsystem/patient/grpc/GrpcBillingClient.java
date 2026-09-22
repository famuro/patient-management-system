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
 * <p>This class isolates Patient Service business logic from the generated
 * protobuf/gRPC client API. PatientService can request billing operations
 * through this adapter without needing to construct protobuf messages or
 * interact directly with the generated gRPC stub.
 *
 * <p>The {@link BillingServiceGrpc.BillingServiceBlockingStub} is created and
 * managed by Spring's gRPC client infrastructure and injected into this class.
 * The blocking stub is appropriate here because patient creation currently
 * performs the Billing Service call synchronously before returning the REST
 * response.
 *
 * <p>If the Billing Service cannot complete the RPC, the resulting gRPC
 * exception is currently allowed to propagate to the caller. More advanced
 * failure handling such as deadlines, retries, or asynchronous event-driven
 * processing can be introduced later as the service architecture evolves.
 */
@Service
public class GrpcBillingClient {
    private static final Logger log = LoggerFactory.getLogger(GrpcBillingClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public GrpcBillingClient(BillingServiceGrpc.BillingServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    /**
     * Creates a billing account for a newly created patient.
     *
     * <p>The patient data is translated into the shared protobuf contract
     * before being sent to the Billing Service through the generated gRPC stub.
     *
     * @param patientId    unique identifier of the patient
     * @param name  patient's name
     * @param email patient's email address
     */
    public void createBillingAccount(String patientId, String name, String email) {
        log.info("Connecting to the gRPC Billing server at channel: {}", blockingStub.getChannel());

        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();

        log.info("Create billing account request: {}", request);

        // The generated blocking stub performs a synchronous RPC. The current
        // request thread waits until Billing returns a response or the call fails.
        BillingResponse response = blockingStub.createBillingAccount(request);

        log.info("Received response from the gRPC Billing server: {}", response);
    }
}
