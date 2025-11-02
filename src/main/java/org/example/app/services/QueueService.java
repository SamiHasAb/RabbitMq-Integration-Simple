package org.example.app.services;

import com.rabbitmq.client.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.example.app.config.RabbitMQProperties;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueueService {

  private Map<String, QueueConfig> configs = new HashMap<>();
  private List<String> queueList = new ArrayList<>();

  @Autowired
  private RabbitMQProperties properties;

  @Autowired
  private Channel channel;


  public Map<String, QueueConfig> getQueuesConfig() {
    return properties.getConfigs();
  }

  public List<String> getAllQueue() {
    Set<String> queueSets = getConfigs().keySet();

    queueSets.forEach(queueSetName -> {
      QueueConfig queueConfig = configs.get(queueSetName);
      queueList.add(queueConfig.getQueue());
      queueList.add(queueConfig.getRetryQueue());
      queueList.add(queueConfig.getDlq());
    });
    return queueList;
  }

  public Map<String, QueueConfig> getConfigs() {
    if (configs.isEmpty()) {
      this.configs = properties.getConfigs();
    }
    return configs;
  }

  public String countMessages(String queueName) {
    boolean queueExists = checkQueueExists(queueName);
    if (!queueExists) {
      log.error("Queue : [{}]  not Found", queueName);
      return String.format("Queue [%s] not found. Check queue configuration.", queueName );
    }
    MessageCountService messageCountService = new MessageCountService(queueName);
    try {
      Long numberOfMessages = messageCountService.doInRabbit(channel);
      return numberOfMessages.toString();
    } catch (Exception e) {
      log.error("Something went wrong.", e);
      return "Can not connect to the rabbit server... Check logs!";
    }
  }

  private boolean checkQueueExists(String queueName) {
    return (getAllQueue().contains(queueName));
  }

  public String peakOnQueue(String queueName) {


  }
}
