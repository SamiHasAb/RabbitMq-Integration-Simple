package org.example.app;

import java.time.LocalTime;
import org.example.app.config.RabbitMQProperties;
import org.example.app.model.Payment;
import org.example.app.sender.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class PaymentAndNotificationController {

  private OrderService orderService;
  private RabbitMQProperties properties;

  public PaymentAndNotificationController(OrderService orderService, RabbitMQProperties properties) {
    this.properties = properties;
    this.orderService = orderService;
  }

  @GetMapping
  public void testPaymentAndNotification() throws InterruptedException {

    System.out.println("\n["+ LocalTime.now()+"] Testing the queues by sending messages......");

    orderService.sendPayment (new Payment("pay123", 49.99 ));

  }

}
