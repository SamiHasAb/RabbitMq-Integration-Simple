package org.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RabbitMqIntegrationApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqIntegrationApplication.class, args);
  }

}
