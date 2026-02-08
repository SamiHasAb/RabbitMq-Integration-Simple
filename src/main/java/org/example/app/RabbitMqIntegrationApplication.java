package org.example.app;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.app.config.RabbitMQProperties;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@Slf4j
@SpringBootApplication
@EnableRetry
public class RabbitMqIntegrationApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext context =  SpringApplication.run(RabbitMqIntegrationApplication.class, args);
    checkPropertiesLoading(context);
  }

  private static void checkPropertiesLoading(ConfigurableApplicationContext context) {
    try {
      RabbitMQProperties props = context.getBean(RabbitMQProperties.class);
      log.info("RabbitMQ Properties loaded successfully");
      log.info("Configs map size: {}", props.getConfigs() != null ? props.getConfigs().size() : 0);

      if (props.getConfigs() != null) {
        props.getConfigs().forEach((key, value) ->
          log.info("Config {}: queue={}, exchange={}", key, value.getQueue(), value.getExchange())
        );
      }
    } catch (Exception e) {
      log.error("Failed to load RabbitMQ properties: {}", e.getMessage());
    }
  }

  @Bean
  public CommandLineRunner checkProperties(RabbitMQProperties props) {
    return args -> {
      log.info("Loaded RabbitMQProperties....");
      Map<String, QueueConfig> configs = props.getConfigs();
      if (props.getConfigs() == null) {
        throw new IllegalStateException("Properties not loaded!");
      }
      configs.forEach((key, value) -> log.info("Queue '{}': {}", key, value));
    };
  }

}
