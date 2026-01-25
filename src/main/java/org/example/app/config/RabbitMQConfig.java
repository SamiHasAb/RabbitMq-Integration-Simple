package org.example.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeUnit;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.example.app.listener.NotificationListener;
import org.example.app.listener.OrderListener;
import org.example.app.listener.PaymentListener;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  private RabbitMQProperties properties;

  public RabbitMQConfig(RabbitMQProperties properties) {
    this.properties = properties;
  }

  // Common Exchanges
  @Bean
  public DirectExchange dlxExchange() {
    return new DirectExchange("dlx.exchange", true, false);
  }

  @Bean
  public DirectExchange retryExchange() {
    return new DirectExchange("retry.exchange", true, false);
  }

  //Queues
  @Bean("paymentQueue")
  public Queue paymentQueue() {
    return buildMainQueue(properties.getPaymentConfig());
  }

  @Bean("notificationQueue")
  public Queue notificationQueue() {
    return buildMainQueue(properties.getNotificationConfig());
  }

  @Bean("orderQueue")
  public Queue orderQueue() {
    return buildMainQueue(properties.getOrderConfig());
  }

  // Add MessageListenerContainers for each queue
  @Bean
  public SimpleMessageListenerContainer paymentListenerContainer(
      ConnectionFactory connectionFactory,
      PaymentListener paymentListener) {

    return createListenerContainer(
        connectionFactory,
        paymentListener,
        "handlePayment", // This should match actual method name
        properties.getPaymentConfig().getQueue()
    );
  }

  @Bean
  public SimpleMessageListenerContainer notificationListenerContainer(
      ConnectionFactory connectionFactory,
      NotificationListener notificationListener) {

    return createListenerContainer(
        connectionFactory,
        notificationListener,
        "notificationListener",
        properties.getNotificationConfig().getQueue()
    );
  }

  @Bean
  public SimpleMessageListenerContainer orderListenerContainer(
      ConnectionFactory connectionFactory,
      OrderListener orderListener) {

    return createListenerContainer(
        connectionFactory,
        orderListener,
        "orderListener",
        properties.getOrderConfig().getQueue()
    );
  }

  private SimpleMessageListenerContainer createListenerContainer(
      ConnectionFactory connectionFactory,
      Object listener,
      String methodName,
      String queueName) {

    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queueName);

    // Create a custom MessageListenerAdapter that handles your method signature
    MessageListenerAdapter adapter = new MessageListenerAdapter(listener) {
      @Override
      protected Object[] buildListenerArguments(Object extractedMessage, Channel channel, Message message) {
        // Return the arguments that match method signature: handlePayment(Message, Channel)
        return new Object[] { message, channel };
      }
    };
    adapter.setMessageConverter(jsonMessageConverter());
    // Configure the adapter to handle your custom method signature
    adapter.setDefaultListenerMethod(methodName);

    container.setMessageListener(adapter);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    container.setAutoStartup(false);
    container.setConcurrentConsumers(1);
    container.setMaxConcurrentConsumers(1);
    container.setPrefetchCount(1);

    // Important: Set these properties for faster shutdown
    container.setForceCloseChannel(true);
    container.setShutdownTimeout(5000); // 5 seconds max for shutdown

    return container;
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

    ObjectMapper mapper = new ObjectMapper();

    mapper.registerModule(new JavaTimeModule());

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // This is important for enum deserialization from string values
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);

    // Set trusted packages
    converter.setClassMapper(classMapper());

    return converter;
  }

  @Bean
  public DefaultClassMapper classMapper() {
    DefaultClassMapper classMapper = new DefaultClassMapper();
//    classMapper.setTrustedPackages("org.example.app.model.*");
    classMapper.setTrustedPackages("*");
    return classMapper;
  }
}