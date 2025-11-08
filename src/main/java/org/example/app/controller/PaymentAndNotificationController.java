package org.example.app.controller;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import org.example.app.config.RabbitMQProperties;
import org.example.app.model.Payment;
import org.example.app.model.PaymentType;
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

    System.out.println("\n[" + LocalTime.now() + "] Testing the queue by sending messages......");

    orderService.sendPayment(new Payment("pay-111", 49.99), PaymentType.CASH);
    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    orderService.sendPayment(new Payment("pay-333", 49.99), PaymentType.CARD);
    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    orderService.sendPayment(new Payment("pay-222", 49.99), PaymentType.SWISH);

//    orderService.sendNotification("email");  // Routes to "notifications" queue
//    orderService.sendOrder("order");  // Routes to "notifications" queue

  }

}
