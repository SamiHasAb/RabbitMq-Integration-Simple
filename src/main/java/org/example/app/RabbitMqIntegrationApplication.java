package org.example.app;

import java.util.Map;
import org.example.app.config.RabbitMQProperties;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RabbitMqIntegrationApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqIntegrationApplication.class, args);
  }

  @Bean
  public CommandLineRunner checkProperties(RabbitMQProperties props) {
    return args -> {
      System.out.println("\nLoaded RabbitMQProperties....");
      Map<String, QueueConfig> configs = props.getConfigs();
      if (props.getConfigs() == null) {
        throw new IllegalStateException("Properties not loaded!");
      }
      configs.forEach((key, value) -> System.out.println(key + " " + value));
    };
  }

}
