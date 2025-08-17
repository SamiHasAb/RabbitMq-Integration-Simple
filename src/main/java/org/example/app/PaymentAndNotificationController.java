package org.example.app;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.example.app.model.Payment;
import org.example.app.sender.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class PaymentAndNotificationController {

  private OrderService orderService;

  public PaymentAndNotificationController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public void testPaymentAndNotification() throws InterruptedException {
    System.out.println("["+ LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)+" ] Sending new payment :");
    orderService.sendPayment (new Payment("pay123", 49.99 ));

  }

}
