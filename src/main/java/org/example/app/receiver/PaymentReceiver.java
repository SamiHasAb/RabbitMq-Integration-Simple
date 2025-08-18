package org.example.app.receiver;

import static org.example.app.sender.OrderService.PAYMENT_METHOD_KEY;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.time.LocalTime;
import org.example.app.config.RabbitMQProperties;
import org.example.app.exception.InvalidPaymentException;
import org.example.app.exception.ServerIsBusyException;
import org.example.app.model.Payment;
import org.example.app.model.PaymentType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
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

      System.out.println(
          "\n[" + LocalTime.now() + "] Received a new payment: \nPayment : [" + payment.toString() + "]  Message ["
              + message.toString() + "] ");

      String paymentMethodValue = message.getMessageProperties().getHeaders().get(PAYMENT_METHOD_KEY).toString();
      processPayment(payment, paymentMethodValue);
      System.out.println("Succeed payment : " + payment);
      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (ServerIsBusyException e) {
      System.err.println("[" + LocalTime.now() + "] Error in processing the payment. " + e.getMessage());
      handleRetry(payment, message, channel);
    } catch (InvalidPaymentException ex) {

      System.err.println("\n[" + LocalTime.now() + "] " + ex.getMessage() + " - send directly to DLQ.");
      channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void processPayment(Payment payment, String paymentMethod) throws Exception {


    if ((paymentMethod).contains(PaymentType.CARD.getMethod())) {
      throw new InvalidPaymentException("Payment processing failed. Invalid payment");
    } else if ((paymentMethod).contains(PaymentType.SWISH.getMethod())) {
      throw new ServerIsBusyException("Payment processing failed. Server is busy");
    }
  }

  private void handleRetry(Payment payment, Message amqpMessage, Channel channel)
      throws IOException {
    Integer retryCount = getRetryCount(amqpMessage);
    System.out.println("[" + LocalTime.now() + "] handleRetry retryCount : [" + (retryCount + 1) + "]");
    String queueType = "payment";

    RabbitMQProperties.QueueConfig config = properties.getConfigs().get(queueType);

    MessageProperties messageProperties = amqpMessage.getMessageProperties();
    if (retryCount >= 4) { // 0-based: 4 retries = 5 total attempts
      // Max retries exceeded - send to DLQ
      System.err.println(
          "\n[" + LocalTime.now() + "] Max retries exceeded - send to DLQ retryCount : [" + (retryCount + 1) + "]\n");
      channel.basicReject(messageProperties.getDeliveryTag(), false);
    } else {
      // Route message to retry queue with updated count

      System.err.println(
          "[" + LocalTime.now() + "] Error in processing the payment. Routing the message to retry queue");
      ;
      retryCount = (retryCount == null) ? 0 : retryCount + 1;
      Integer finalRetryCount = retryCount;
      rabbitTemplate.convertAndSend(
          config.getRetryExchange(),
          config.getRetryRoutingKey(),
          payment,
          msg -> {
            msg.getMessageProperties().setHeader("x-retry-count", finalRetryCount);
            msg.getMessageProperties().setHeader(PAYMENT_METHOD_KEY, messageProperties.getHeaders().get(PAYMENT_METHOD_KEY));
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