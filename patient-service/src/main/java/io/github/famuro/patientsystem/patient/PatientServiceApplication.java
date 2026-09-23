package io.github.famuro.patientsystem.patient;

import io.github.famuro.patientsystem.contract.billing.v1.BillingServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

/**
 * Entry point for the Patient Service application.
 *
 * <p>Bootstraps the Spring application and registers the Billing gRPC client.
 */
@SpringBootApplication
@ImportGrpcClients(target = "billing", types = BillingServiceGrpc.BillingServiceBlockingStub.class)
public class PatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
    }

}
