package com.infopharma.ipos_sa;

/**
 * IposSaApplication
 * Spring Boot entry point for the IPOS-SA server application.
 * {@code @EnableScheduling} activates the nightly {@code AccountScheduler}
 * that automatically flags overdue merchant accounts.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Required for AccountScheduler nightly status updates
public class IposSaApplication {

    public static void main(String[] args) {
        SpringApplication.run(IposSaApplication.class, args);
    }
}
