package org.example.app.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.example.app.config.RabbitMQProperties;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.example.app.model.QueueLookupResult;
import org.example.app.model.QueueLookupResult.QueueType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class QueueService {

  private Map<String, QueueConfig> configs = new HashMap<>();
  private List<String> queueList = new ArrayList<>();

  @Autowired
  private RabbitMQProperties properties;

  @Autowired
  private RabbitTemplate rabbitTemplate;

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
      return String.format("Queue [%s] not found. Check queue configuration.", queueName);
    }
    MessageCountService messageCountService = new MessageCountService(queueName);
    try {
      Long numberOfMessages = rabbitTemplate.execute(messageCountService);
      return String.format("There are [%d] message(s) in [%s]", numberOfMessages, queueName);
    } catch (Exception e) {
      log.error("Something went wrong.", e);
      return "Can not connect to rabbit server... Check logs!";
    }
  }

  private boolean checkQueueExists(String queueName) {
    return (getAllQueue().contains(queueName));
  }

  public String peakOnQueue(String queueName) {
    boolean queueExists = checkQueueExists(queueName);
    if (!queueExists) {
      log.error("Queue : [{}]  not Found", queueName);
      return String.format("Queue [%s] not found. Check queue configuration.", queueName);
    }
    MessagePeakService messagePeakService = new MessagePeakService(queueName);
    Message message = rabbitTemplate.execute(messagePeakService);
    if (ObjectUtils.isEmpty(message)) {
      return String.format("There are no messages in the %s. Check the queue count endpoint.", queueName);
    }
    return new String(message.getBody(), StandardCharsets.UTF_8);
  }

  public List<String> moveMessage(String queueName, int numberOfMessages) {
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<String> collection = new ArrayList<>();
    boolean moveToDLQ;
    Message receivedMessage;

    Optional<QueueLookupResult> resultOpt = findQueueConfig(queueName);

    String finalQueueName = queueName;

    if (resultOpt.isEmpty()) {
      System.out.println("Queue not found: " + finalQueueName);
      throw new IllegalArgumentException(String.format("Queue not [%s] found", queueName));
    } else {
      QueueLookupResult result = resultOpt.get();
      boolean isDefault = result.getType() == QueueType.DEFAULT;

      if (isDefault) {
        System.out.println("Found in default queues: " + result.getEntry().getKey());
      } else {
        System.out.println("Found in DLQs: " + result.getEntry().getValue().getDlq());
      }

      moveToDLQ = isDefault;
    }

    Entry<String, QueueConfig> queueConfig = getQueueConfig(queueName, moveToDLQ);
    queueName = queueConfig.getValue().getQueue();
    String exchangeName = queueConfig.getValue().getExchange();
    String dlqExchangeName = queueConfig.getValue().getDlxExchange();
    String dlxRoutingKey = queueConfig.getValue().getDlxRoutingKey();
    String deadLetterQueueName = queueConfig.getValue().getDlq();

    if (moveToDLQ) {
      int number;
      for (number = 0; number < numberOfMessages; ++number) {
        receivedMessage = rabbitTemplate.receive(queueName);
        if (ObjectUtils.isEmpty(receivedMessage)) {
          messages.add("No more messages in queue");
          break;
        }
        String receivedRoutingKey = receivedMessage.getMessageProperties().getReceivedRoutingKey();
        log.info("Received message from [{}] queue\nMessage : {}", queueName,
            new String(receivedMessage.getBody(), StandardCharsets.UTF_8));
        postInDeadLetterQueue(messages, deadLetterQueueName, dlxRoutingKey,receivedMessage, dlqExchangeName, receivedRoutingKey);
      }
      messages.add(
          String.format("%s messages moved from queue: [%s] to queue [%s]", number, queueName, deadLetterQueueName));
      collection.add(StringUtils.join(String.valueOf(messages), ", "));
      return collection;
    } else {
      int number;
      for (number = 0; number < numberOfMessages; ++number) {
        receivedMessage = rabbitTemplate.receive(deadLetterQueueName);
        if (ObjectUtils.isEmpty(receivedMessage)) {
          messages.add("No more messages in the dead letter queue");
          break;
        }
        log.info("Received a message from [{}]\nMessage : {}", deadLetterQueueName,
            new String(receivedMessage.getBody(), StandardCharsets.UTF_8));
        postInOriginalExchange(messages, queueName, exchangeName, receivedMessage);
      }
      messages.add(
          String.format("%s messages moved from queue: [%s] to queue [%s]", number, deadLetterQueueName, queueName));
      collection.add(StringUtils.join(String.valueOf(messages), ", "));

      return collection;
    }
  }

  private void postInDeadLetterQueue(ArrayList<String> messages, String dlq,String dlxRoutingKey, Message receivedMessage,
      String exchangeName, String receivedRoutingKey) {
    //TODO: Check if the xDeathHeader is added.
    //getQueueName
    MessageProperties receivedMessageProperties = receivedMessage.getMessageProperties();


    List<HashMap<String, List<String>>> xDeathHeader = getXDeath(receivedRoutingKey, "Original Queue name");
    receivedMessageProperties.setHeader("x-death", xDeathHeader);


    log.info("Moving message from [{}] to [{}]\n RoutingKey: [{}]", new Object[]{"Default Queue name", dlq, dlxRoutingKey});
    messages.add(new String(receivedMessage.getBody(), StandardCharsets.UTF_8));
    rabbitTemplate.send(exchangeName, dlxRoutingKey, new Message(receivedMessage.getBody(), receivedMessageProperties));

  }

  private void postInOriginalExchange(ArrayList<String> messages, String queueName, String exchangeName,
      Message receivedMessage) {
    MessageProperties receivedMessageMessageProperties = receivedMessage.getMessageProperties();
    MessageProperties defaultProperties = getDefaultProperties();
    Map<String, Object> headers = receivedMessageMessageProperties.getHeaders();
    defaultProperties.setCorrelationId(receivedMessageMessageProperties.getCorrelationId());
    String routingKey = (String) headers.get("routingKey");
    Object xDeathHeader = headers.get("x-death");
    HashMap<String, Object> map = (HashMap) ((List) xDeathHeader).get(0);
    if (map.containsKey("routing-keys")) {
      routingKey = (String) ((List) map.get("routing-keys")).get(0);
    }

    log.info("Moving message from Dead Letter queue {}.\nRoutingKey: [{}]", "DLQ for " + queueName, routingKey);
    messages.add(new String(receivedMessage.getBody(), StandardCharsets.UTF_8));
    System.out.println("exchangeName : " + exchangeName);
    System.out.println("rountingKey : " + routingKey);
    System.out.println("defaultProperties : " + defaultProperties.toString());
    System.out.println("ReceivedMessage Body: " + receivedMessage.getBody().toString()); //Fix this
    rabbitTemplate.send(exchangeName, routingKey, new Message(receivedMessage.getBody(), defaultProperties));
  }

  private MessageProperties getDefaultProperties() {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setContentType("application/json");
    messageProperties.setAppId("RabbitMq Integration App");
    return messageProperties;
  }

  private Entry<String, QueueConfig> getQueueConfig(String queueName, Boolean moveToDLQ) {
    if (moveToDLQ) {
      return getConfigs().entrySet().stream()
          .filter(entry -> entry.getValue().getQueue().equalsIgnoreCase(queueName))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(String.format("Queue not [%s] found", queueName)));
    } else {
      //then it is a dlq as a queueName
      return getConfigs().entrySet().stream()
          .filter(entry -> entry.getValue().getDlq().equalsIgnoreCase(queueName))
          .findFirst()
          .orElseThrow(
              () -> new IllegalArgumentException(String.format("Dead Letter Queue not [%s] found", queueName)));
    }


  }

  private Entry<String, QueueConfig> getQueueConfigInDefaultQueues(String queueName) {

    return getConfigs().entrySet().stream()
        .filter(entry -> entry.getKey().equalsIgnoreCase(queueName))
        .findFirst()
        .orElse(null);
  }

  private Entry<String, QueueConfig> getQueueConfigInDlqs(String queueName) {

    //then it is a dlq as a queueName
    return getConfigs().entrySet().stream()
        .filter(entry -> entry.getValue().getDlq().equalsIgnoreCase(queueName))
        .findFirst()
        .orElse(null);
  }

  private Optional<QueueLookupResult> findQueueConfig(String queueName) {
    // Try default queues
    Optional<Entry<String, QueueConfig>> defaultQueue = getConfigs().entrySet().stream()
        .filter(entry -> entry.getValue().getQueue().equalsIgnoreCase(queueName))
        .findFirst();

    if (defaultQueue.isPresent()) {
      return Optional.of(new QueueLookupResult(defaultQueue.get(), QueueLookupResult.QueueType.DEFAULT));
    }

    // Try DLQs
    Optional<Entry<String, QueueConfig>> dlq = getConfigs().entrySet().stream()
        .filter(entry -> entry.getValue().getDlq().equalsIgnoreCase(queueName))
        .findFirst();

    if (dlq.isPresent()) {
      return Optional.of(new QueueLookupResult(dlq.get(), QueueLookupResult.QueueType.DLQ));
    }

    // Not found
    return Optional.empty();
  }


  private List<HashMap<String, List<String>>> getXDeath(String receivedRoutingKey, String originalQueue) {
    HashMap<String, List<String>> XHeaderInfo = new HashMap<>();
    XHeaderInfo.put("routing-keys", List.of(receivedRoutingKey));
    XHeaderInfo.put("queue", List.of(originalQueue));
    XHeaderInfo.put("reason", List.of("Manual rejection via api call"));
    return List.of(XHeaderInfo);
  }
}
