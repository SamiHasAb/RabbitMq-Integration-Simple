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

  public void sendPayment(Payment payment) {
    rabbitTemplate.convertAndSend(
        properties.getExchange(),
        properties.getRoutingKey(),
        payment
    );
  }

}


