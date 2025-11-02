package org.example.app.services;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.util.ObjectUtils;

@Slf4j
public class MessagePeakService implements ChannelCallback<Message> {

  private final static String DEFAULT_ENCODING = "UTF-8";
  private final String queueName;
  private MessagePropertiesConverter propertiesConverter = new DefaultMessagePropertiesConverter();

  public MessagePeakService(String queueName) {
    this.queueName = queueName;
  }

  @Override
  public Message doInRabbit(Channel channel) throws Exception {

    GetResponse response = channel.basicGet(queueName, false);

    if (ObjectUtils.isEmpty(response)) {
      // No message found
      return null;
    }
    BasicProperties props = response.getProps();
    Envelope envelope = response.getEnvelope();
    byte[] responseBody = response.getBody();

    channel.basicNack(envelope.getDeliveryTag(), true, true);

    log.info("Peaked on [{}] \nMessage body [{}]", queueName, new String(responseBody));
    MessageProperties messageProperties = propertiesConverter.toMessageProperties(props, envelope, DEFAULT_ENCODING);
    return new Message(responseBody, messageProperties);
  }
}
