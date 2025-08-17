package org.example.app.config;

import java.util.concurrent.TimeUnit;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Autowired
  private RabbitMQProperties properties;

  // Common Exchanges (declare them as separate beans first)
  @Bean
  public DirectExchange dlxExchange() {
    return new DirectExchange("dlx.exchange", true, false);
  }

  @Bean
  public DirectExchange retryExchange() {
    return new DirectExchange("retry.exchange", true, false);
  }

  // Main Queue Beans (for @RabbitListener injection)
  @Bean
  public Queue paymentQueue() {
    return buildMainQueue(properties.getPaymentConfig());
  }

  @Bean
  public Queue notificationQueue() {
    return buildMainQueue(properties.getNotificationConfig());
  }

  @Bean
  public Queue orderQueue() {
    return buildMainQueue(properties.getOrderConfig());
  }

  // Exchange and Queue Declarations with Bindings
  @Bean
  public Declarables rabbitBindings() {
    return new Declarables(
        // Payment Exchange
        new DirectExchange(properties.getPaymentConfig().getExchange(), true, false),

        // Payment Bindings
        new Binding(
            properties.getPaymentConfig().getQueue(),
            Binding.DestinationType.QUEUE,
            properties.getPaymentConfig().getExchange(),
            properties.getPaymentConfig().getRoutingKey(),
            null),

        // Payment Retry Queue
        buildRetryQueue(properties.getPaymentConfig()),

        // Payment Retry Binding
        new Binding(
            properties.getPaymentConfig().getRetryQueue(),
            Binding.DestinationType.QUEUE,
            properties.getPaymentConfig().getRetryExchange(),
            properties.getPaymentConfig().getRetryRoutingKey(),
            null),

        // Payment DLQ
        new Queue(properties.getPaymentConfig().getDlq(), true),

        // Payment DLQ Binding
        new Binding(
            properties.getPaymentConfig().getDlq(),
            Binding.DestinationType.QUEUE,
            properties.getPaymentConfig().getDlxExchange(),
            properties.getPaymentConfig().getDlxRoutingKey(),
            null),

        // Repeat the same pattern for notification and order...
        // Notification Exchange
        new DirectExchange(properties.getNotificationConfig().getExchange(), true, false),

        // Notification Bindings
        new Binding(
            properties.getNotificationConfig().getQueue(),
            Binding.DestinationType.QUEUE,
            properties.getNotificationConfig().getExchange(),
            properties.getNotificationConfig().getRoutingKey(),
            null),

        // Notification Retry Queue
        buildRetryQueue(properties.getNotificationConfig()),

        // Notification Retry Binding
        new Binding(
            properties.getNotificationConfig().getRetryQueue(),
            Binding.DestinationType.QUEUE,
            properties.getNotificationConfig().getRetryExchange(),
            properties.getNotificationConfig().getRetryRoutingKey(),
            null),

        // Notification DLQ
        new Queue(properties.getNotificationConfig().getDlq(), true),

        // Notification DLQ Binding
        new Binding(
            properties.getNotificationConfig().getDlq(),
            Binding.DestinationType.QUEUE,
            properties.getNotificationConfig().getDlxExchange(),
            properties.getNotificationConfig().getDlxRoutingKey(),
            null),

        // Order Exchange
        new DirectExchange(properties.getOrderConfig().getExchange(), true, false),

        // Order Bindings
        new Binding(
            properties.getOrderConfig().getQueue(),
            Binding.DestinationType.QUEUE,
            properties.getOrderConfig().getExchange(),
            properties.getOrderConfig().getRoutingKey(),
            null),

        // Order Retry Queue
        buildRetryQueue(properties.getOrderConfig()),

        // Order Retry Binding
        new Binding(
            properties.getOrderConfig().getRetryQueue(),
            Binding.DestinationType.QUEUE,
            properties.getOrderConfig().getRetryExchange(),
            properties.getOrderConfig().getRetryRoutingKey(),
            null),

        // Order DLQ
        new Queue(properties.getOrderConfig().getDlq(), true),

        // Order DLQ Binding
        new Binding(
            properties.getOrderConfig().getDlq(),
            Binding.DestinationType.QUEUE,
            properties.getOrderConfig().getDlxExchange(),
            properties.getOrderConfig().getDlxRoutingKey(),
            null)
    );
  }

  private Queue buildMainQueue(QueueConfig config) {
    return QueueBuilder.durable(config.getQueue())
        .withArgument("x-dead-letter-exchange", config.getDlxExchange())
        .withArgument("x-dead-letter-routing-key", config.getDlxRoutingKey())
        .build();
  }

  private Queue buildRetryQueue(QueueConfig config) {
    return QueueBuilder.durable(config.getRetryQueue())
        .withArgument("x-dead-letter-exchange", config.getExchange())
        .withArgument("x-dead-letter-routing-key", config.getRoutingKey())
        .withArgument("x-message-ttl", TimeUnit.SECONDS.toMillis(10)) // 10 seconds delay
        .build();
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}