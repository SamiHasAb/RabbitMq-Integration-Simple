package org.example.app.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.app.config.RabbitMQProperties;
import org.example.app.exception.InvalidPaymentException;
import org.example.app.exception.ServerIsBusyException;
import org.example.app.model.Payment;
import org.example.app.model.PaymentType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentListener {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitMQProperties properties;

  @Autowired
  private ObjectMapper objectMapper;

  public void handlePayment(Message message, Channel channel)
      throws IOException {
    try {
      Payment payment = convertMessageToPayment(message);

      log.info("Received a new payment: Payment : [{}]", payment);

      processPayment(payment.getPaymentType());
      log.info("Succeed payment : [{}]", payment);
      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (ServerIsBusyException e) {
      log.error("Error in processing the payment. {}", e.getMessage());
      handleRetry( message, channel);
    } catch (InvalidPaymentException ex) {
      log.error("{} - send directly to DLQ.", ex.getMessage());
      channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    } catch (IllegalArgumentException e) {
      log.error("Corrupted message :{}", e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Payment convertMessageToPayment(Message message) throws IOException {
    try {
      // Try direct conversion first
      Object converted = rabbitTemplate.getMessageConverter().fromMessage(message);
      if (converted instanceof Payment) {
        return (Payment) converted;
      } else if (converted instanceof Map) {
        // Convert Map to Payment using ObjectMapper
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) converted;

        // Convert paymentType string to enum
        if (map.containsKey("paymentType")) {
          String paymentTypeStr = map.get("paymentType").toString();
          map.put("paymentType", PaymentType.valueOf(paymentTypeStr));
        }
        return objectMapper.convertValue(map, Payment.class);
      } else {
        throw new IllegalArgumentException("Cannot convert message to Payment: " + converted.getClass());
      }
    } catch (Exception e) {
      // Fallback: Try to deserialize from JSON string
      String json = new String(message.getBody());
      return objectMapper.readValue(json, Payment.class);
    }
  }

  private void processPayment(PaymentType paymentType) {
    if (paymentType == null) {
      throw new IllegalArgumentException("payment method can not be null");
    }
    switch(paymentType) {
      case CARD -> throw new InvalidPaymentException("Payment processing failed. Invalid payment");
      case SWISH -> throw new ServerIsBusyException("Payment processing failed. Server is busy");
      case CASH -> log.info("Cash payment processed");
    }
  }

  private void handleRetry(Message amqpMessage, Channel channel)
      throws IOException {
    Integer retryCount = getRetryCount(amqpMessage);
    log.info("handleRetry retryCount: [{}]", retryCount + 1);

    String queueType = "payment";
    Payment payment = convertMessageToPayment(amqpMessage);
    RabbitMQProperties.QueueConfig config = properties.getConfigs().get(queueType);

    MessageProperties messageProperties = amqpMessage.getMessageProperties();
    if (retryCount >= 4) { // 0-based: 4 retries = 5 total attempts
      // Max retries exceeded - send to DLQ
      log.error("Max retries exceeded - send to DLQ retryCount: [{}]", retryCount + 1);
      channel.basicReject(messageProperties.getDeliveryTag(), false);
    } else {
      // Route message to retry queue with updated count
      log.error("Error in processing the payment. Routing the message to retry queue");
      retryCount = (retryCount == null) ? 0 : retryCount + 1;
      Integer finalRetryCount = retryCount;
      rabbitTemplate.convertAndSend(
          config.getRetryExchange(),
          config.getRetryRoutingKey(),
          payment,
          msg -> {
            msg.getMessageProperties().setHeader("x-retry-count", finalRetryCount);
            return msg;
          }
      );
      channel.basicAck(messageProperties.getDeliveryTag(), false);
    }
  }

  private Integer getRetryCount(Message message) {
    return (Integer) message.getMessageProperties()
        .getHeaders()
        .getOrDefault("x-retry-count", 0);
  }
}