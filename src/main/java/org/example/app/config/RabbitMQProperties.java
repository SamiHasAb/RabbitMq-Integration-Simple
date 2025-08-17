package org.example.app.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {

  private Map<String, QueueConfig> configs;


  public static class QueueConfig {

    private String exchange;
    private String queue;
    private String routingKey;
    private String dlxExchange;
    private String dlq;
    private String dlxRoutingKey;
    private String retryExchange;
    private String retryQueue;
    private String retryRoutingKey;

    // Getters and Setters
    public String getExchange() {
      return exchange;
    }

    public void setExchange(String exchange) {
      this.exchange = exchange;
    }

    public String getQueue() {
      return queue;
    }

    public void setQueue(String queue) {
      this.queue = queue;
    }

    public String getRoutingKey() {
      return routingKey;
    }

    public void setRoutingKey(String routingKey) {
      this.routingKey = routingKey;
    }

    public String getDlxExchange() {
      return dlxExchange;
    }

    public void setDlxExchange(String dlxExchange) {
      this.dlxExchange = dlxExchange;
    }

    public String getDlq() {
      return dlq;
    }

    public void setDlq(String dlq) {
      this.dlq = dlq;
    }

    public String getDlxRoutingKey() {
      return dlxRoutingKey;
    }

    public void setDlxRoutingKey(String dlxRoutingKey) {
      this.dlxRoutingKey = dlxRoutingKey;
    }

    public String getRetryExchange() {
      return retryExchange;
    }

    public void setRetryExchange(String retryExchange) {
      this.retryExchange = retryExchange;
    }

    public String getRetryQueue() {
      return retryQueue;
    }

    public void setRetryQueue(String retryQueue) {
      this.retryQueue = retryQueue;
    }

    public String getRetryRoutingKey() {
      return retryRoutingKey;
    }

    public void setRetryRoutingKey(String retryRoutingKey) {
      this.retryRoutingKey = retryRoutingKey;
    }

    @Override
    public String toString() {
      return "QueueConfig{" +
          "exchange='" + exchange + '\'' +
          ", queue='" + queue + '\'' +
          ", routingKey='" + routingKey + '\'' +
          ", dlxExchange='" + dlxExchange + '\'' +
          ", dlq='" + dlq + '\'' +
          ", dlxRoutingKey='" + dlxRoutingKey + '\'' +
          ", retryExchange='" + retryExchange + '\'' +
          ", retryQueue='" + retryQueue + '\'' +
          ", retryRoutingKey='" + retryRoutingKey + '\'' +
          '}';
    }
  }

  // Getters and Setters
  public Map<String, QueueConfig> getConfigs() {
    return configs;
  }

  public void setConfigs(Map<String, QueueConfig> configs) {
    this.configs = configs;
  }

  public QueueConfig getPaymentConfig() {
    return getConfig("payment");
  }

  public QueueConfig getNotificationConfig() {
    return getConfig("notification");
  }

  public QueueConfig getOrderConfig() {
    return getConfig("order");
  }

  private QueueConfig getConfig(String type) {
    if (configs == null || !configs.containsKey(type)) {
      throw new IllegalStateException("Missing RabbitMQ configuration for: " + type);
    }
    return configs.get(type);
  }
}
