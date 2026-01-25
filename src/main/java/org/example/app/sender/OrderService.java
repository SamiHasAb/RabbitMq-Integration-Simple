package org.example.app.sender;


import org.example.app.config.RabbitMQProperties;
import org.example.app.model.Payment;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitMQProperties properties;

//TODO: Move to payment Service
  public void sendPayment(Payment payment) {

    String queueType = "payment";
    RabbitMQProperties.QueueConfig config = properties.getConfigs().get(queueType);

    if (config == null) {
      throw new IllegalArgumentException("Unknown queue type: " + queueType);
    }

    rabbitTemplate.convertAndSend(
        config.getExchange(),
        config.getRoutingKey(),
        payment);

    }
  }