package org.example.app;

import org.example.app.config.RabbitMQProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RabbitMqIntegrationApplication {
  private static final Logger logger = LoggerFactory.getLogger(RabbitMqIntegrationApplication.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext context =  SpringApplication.run(RabbitMqIntegrationApplication.class, args);
    checkPropertiesLoading(context);
  }

  private static void checkPropertiesLoading(ConfigurableApplicationContext context) {
    try {
      RabbitMQProperties props = context.getBean(RabbitMQProperties.class);
      logger.info("RabbitMQ Properties loaded successfully");
      logger.info("Configs map size: {}", props.getConfigs() != null ? props.getConfigs().size() : 0);

      if (props.getConfigs() != null) {
        props.getConfigs().forEach((key, value) ->
          logger.info("Config {}: queue={}, exchange={}", key, value.getQueue(), value.getExchange())
        );
      }
    } catch (Exception e) {
      logger.error("Failed to load RabbitMQ properties: {}", e.getMessage());
    }
  }

}
