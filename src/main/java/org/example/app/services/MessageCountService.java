package org.example.app.services;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.ChannelCallback;

public class MessageCountService implements ChannelCallback<Long> {

  private final String queueName;

  public MessageCountService(String queueName) {
    this.queueName = queueName;
  }

  @Override
  public Long doInRabbit(Channel channel) throws Exception {
    return channel.messageCount(queueName);
  }
}
