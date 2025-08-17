package org.example.app.config;

//import static org.example.app.receiver.OrderListener.MAX_RETRIES;
//import static org.example.app.receiver.OrderListener.RETRY_DELAY_MS;

import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


  @Autowired
  private RabbitMQProperties properties;

  @Bean
  DirectExchange paymentExchange() {
    return new DirectExchange(properties.getExchange());
  }

  @Bean
  Queue paymentQueue() {
    return QueueBuilder.durable(properties.getQueue())
        .withArgument("x-dead-letter-exchange", properties.getDlxExchange())
        .withArgument("x-dead-letter-routing-key", properties.getDlxRoutingKey())
        .build();
  }

  @Bean
  Binding paymentBinding() {
    return BindingBuilder.bind(paymentQueue())
        .to(paymentExchange())
        .with(properties.getRoutingKey());
  }

  @Bean
  DirectExchange dlxExchange() {
    return new DirectExchange(properties.getDlxExchange());
  }

  @Bean
  Queue dlqQueue() {
    return new Queue(properties.getDlq(), true);
  }


  @Bean
  Binding dlqBinding() {
    return BindingBuilder.bind(dlqQueue())
        .to(dlxExchange())
        .with(properties.getDlxRoutingKey());
  }

  @Bean
  DirectExchange retryExchange() {
    return new DirectExchange(properties.getRetryExchange());
  }

  @Bean
  Queue retryQueue() {
    return QueueBuilder.durable(properties.getRetryQueue())
        .withArgument("x-dead-letter-exchange", properties.getExchange())
        .withArgument("x-dead-letter-routing-key", properties.getRoutingKey())
        .withArgument("x-message-ttl", TimeUnit.SECONDS.toMillis(10)) // 10 seconds delay
        .build();
  }


  @Bean
  Binding retryBinding() {
    return BindingBuilder.bind(retryQueue())
        .to(retryExchange())
        .with(properties.getRetryRoutingKey());
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
