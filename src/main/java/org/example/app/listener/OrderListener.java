package org.example.app.listener;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

@Service
public class OrderListener {

  public void handlePayment(Message message, Channel channel)
      throws IOException {

  }
}
