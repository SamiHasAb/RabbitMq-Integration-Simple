package org.example.app.receiver;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.time.LocalTime;
import org.example.app.config.RabbitMQProperties;
import org.example.app.model.Payment;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentReceiver {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitMQProperties properties;

  @RabbitListener(queues = "#{@paymentQueue}")
  public void receivePayment(Payment payment, Message message, Channel channel)
      throws IOException {
    try {
      // Simulate payment processing (throw exception to simulate failure)

      System.out.println("\n["+ LocalTime.now()+"] Received a payment: Payment : ["+payment.toString()+"]  Message ["+message.toString()+"] ");
      processPayment(payment);
      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
      System.err.println("["+ LocalTime.now()+"] Error in processing the payment");
      handleRetry(payment, message, channel);
    }
  }

  private void processPayment(Payment payment) throws Exception {
    // Add your business logic here
    // Throw exception to simulate processing failure
    throw new Exception("Payment processing failed");
  }

  private void handleRetry(Payment payment, Message amqpMessage, Channel channel)
      throws IOException {
    Integer retryCount = getRetryCount(amqpMessage);
    System.out.println("["+ LocalTime.now()+"] handleRetry retryCount : ["+(retryCount+1) +"]");
    String queueType = "payment";

    RabbitMQProperties.QueueConfig config = properties.getConfigs().get(queueType);

    if (retryCount >= 4) { // 0-based: 4 retries = 5 total attempts
      // Max retries exceeded - send to DLQ
      System.err.println("\n["+ LocalTime.now()+"] Max retries exceeded - send to DLQ retryCount : ["+(retryCount+1)+"]\n");
      channel.basicReject(amqpMessage.getMessageProperties().getDeliveryTag(), false);
    } else {
      // Route message to retry queue with updated count

      System.err.println("["+ LocalTime.now()+"] Error in processing the payment. Routing the message to retry queue");

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
      channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
    }
  }

  private Integer getRetryCount(Message message) {
    return (Integer) message.getMessageProperties()
        .getHeaders()
        .getOrDefault("x-retry-count", 0);
  }
}