package org.example.app.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.example.app.config.RabbitMQProperties;
import org.example.app.model.ListenerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitListenerService {

  public static final String NOTIFICATION_LISTENER_ID = "notificationListener";
  public static final String PAYMENT_LISTENER_ID = "paymentListener";
  public static final String ORDER_LISTENER_ID = "orderListener";


  private final Map<String, SimpleMessageListenerContainer> listenerContainers;
  private final Map<String, ListenerInfo> listenerInfoMap = new ConcurrentHashMap<>();
  private final RabbitMQProperties rabbitMQProperties;
  private static final Logger logger = LoggerFactory.getLogger(RabbitListenerService.class);


  @Autowired
  public RabbitListenerService(
      SimpleMessageListenerContainer paymentListenerContainer,
      SimpleMessageListenerContainer notificationListenerContainer,
      SimpleMessageListenerContainer orderListenerContainer,
      RabbitMQProperties rabbitMQProperties
  ) {
    this.rabbitMQProperties = rabbitMQProperties;
    this.listenerContainers = new HashMap<>();
    this.listenerContainers.put(PAYMENT_LISTENER_ID, paymentListenerContainer);
    this.listenerContainers.put(NOTIFICATION_LISTENER_ID, notificationListenerContainer);
    this.listenerContainers.put(ORDER_LISTENER_ID, orderListenerContainer);

    initializeListenerInfo();
    logger.info("Listener service initialized. Auto-start enabled for all listeners.");
  }

  private void initializeListenerInfo() {
    try {
      // Safe initialization with fallbacks
      String paymentQueue = getQueueNameSafely("payment", "payment.queue");
      String notificationQueue = getQueueNameSafely("notification", "notification.queue");
      String orderQueue = getQueueNameSafely("order", "order.queue");

      listenerInfoMap.put(PAYMENT_LISTENER_ID, new ListenerInfo(
          paymentQueue,
          PAYMENT_LISTENER_ID,
          listenerContainers.get(PAYMENT_LISTENER_ID).isRunning(),
          LocalDateTime.now()));

      listenerInfoMap.put(NOTIFICATION_LISTENER_ID, new ListenerInfo(
          notificationQueue,
          NOTIFICATION_LISTENER_ID,
          listenerContainers.get(NOTIFICATION_LISTENER_ID).isRunning(),
          LocalDateTime.now()
      ));

      listenerInfoMap.put(ORDER_LISTENER_ID, new ListenerInfo(
          orderQueue,
          ORDER_LISTENER_ID,
          listenerContainers.get(ORDER_LISTENER_ID).isRunning(),
          LocalDateTime.now()
      ));

      logger.info("All listeners initialized and running: Payment={}, Notification={}, Order={}",
          listenerContainers.get(PAYMENT_LISTENER_ID).isRunning(),
          listenerContainers.get(NOTIFICATION_LISTENER_ID).isRunning(),
          listenerContainers.get(ORDER_LISTENER_ID).isRunning());

    } catch (Exception e) {
      logger.error("Failed to initialize listener info from properties, using defaults", e);
      initializeWithDefaultQueues();
    }
  }


  private String getQueueNameSafely(String configType, String defaultName) {
    try {
      if (rabbitMQProperties.getConfigs() != null && rabbitMQProperties.getConfigs().containsKey(configType)) {
        RabbitMQProperties.QueueConfig config = rabbitMQProperties.getConfigs().get(configType);
        return config.getQueue() != null ? config.getQueue() : defaultName;
      }
    } catch (Exception e) {
      logger.warn("Failed to get queue name for {}: {}", configType, e.getMessage());
    }
    return defaultName;
  }

  private void initializeWithDefaultQueues() {
    listenerInfoMap.put(PAYMENT_LISTENER_ID, new ListenerInfo(
        "payment.queue",
        PAYMENT_LISTENER_ID,
        true,
        LocalDateTime.now()));

    listenerInfoMap.put(NOTIFICATION_LISTENER_ID, new ListenerInfo(
        "notification.queue",
        NOTIFICATION_LISTENER_ID,
        true,
        LocalDateTime.now()
    ));

    listenerInfoMap.put(ORDER_LISTENER_ID, new ListenerInfo(
        "order.queue",
        ORDER_LISTENER_ID,
        true,
        LocalDateTime.now()
    ));
  }

  public void stopListener(String listenerId) {
    SimpleMessageListenerContainer container = listenerContainers.get(listenerId);
    if (container != null && container.isRunning()) {
      // Force stop by canceling consumers and stopping container
      container.stop();
      // Wait a bit for current processing to complete
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      updateListenerStatus(listenerId, false);
      logger.info("Forcefully stopped listener: {}", listenerId);
    }
  }

  public void startListener(String listenerId) {
    SimpleMessageListenerContainer container = listenerContainers.get(listenerId);
    if (container != null && !container.isRunning()) {
      container.start();
      updateListenerStatus(listenerId, true);
      logger.info("Started listener: {}", listenerId);
    }
  }

  public ListenerInfo getListenerStatus(String listenerId) {
    SimpleMessageListenerContainer container = listenerContainers.get(listenerId);
    ListenerInfo info = listenerInfoMap.get(listenerId);

    if (container != null && info != null) {
      return new ListenerInfo(
          listenerId,
          container.isRunning(),
          info.getStartedAt(),
          info.getQueueName(),
          container.getActiveConsumerCount(),
          container.getQueueNames()
      );
    }
    return null;
  }

  public List<ListenerInfo> getAllListenersStatus() {
    return listenerContainers.keySet().stream()
        .map(this::getListenerStatus)
        .filter(Objects::nonNull)
        .toList();
  }

  private void updateListenerStatus(String listenerId, boolean isRunning) {
    ListenerInfo currentInfo = listenerInfoMap.get(listenerId);
    if (currentInfo != null) {
      LocalDateTime startedAt = isRunning ? LocalDateTime.now() : currentInfo.getStartedAt();
      SimpleMessageListenerContainer container = listenerContainers.get(listenerId);

      listenerInfoMap.put(listenerId, new ListenerInfo(
          listenerId,
          isRunning,
          startedAt,
          currentInfo.getQueueName(),
          container != null ? container.getActiveConsumerCount() : 0,
          container != null ? container.getQueueNames() : new String[0]
      ));
    }
  }

  public void forceStopListener(String listenerId) {
    SimpleMessageListenerContainer container = listenerContainers.get(listenerId);
    if (container != null) {
      logger.info("=== FORCE STOPPING {} ===", listenerId);
      logger.info("Before: isRunning={}, isActive={}, Consumers={}",
          container.isRunning(), container.isActive(), container.getActiveConsumerCount());

      // Method 1: Regular stop
      container.stop();

      // Method 2: If still running, use more forceful approach
      if (container.isRunning()) {
        logger.warn("Container still running after stop(), using direct channel cancellation");
        // Force cancel consumers
        container.destroy();
      }

      // Wait and verify
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      logger.info("After: isRunning={}, isActive={}, Consumers={}",
          container.isRunning(), container.isActive(), container.getActiveConsumerCount());
      logger.info("=== FORCE STOP COMPLETE FOR {} ===", listenerId);

      updateListenerStatus(listenerId, false);
    }
  }

  public Map<String, Object> getContainerDetails(String listenerId) {
    SimpleMessageListenerContainer container = listenerContainers.get(listenerId);
    if (container != null) {
      Map<String, Object> details = new HashMap<>();
      details.put("listenerId", listenerId);
      details.put("isRunning", container.isRunning());
      details.put("isActive", container.isActive());
      details.put("activeConsumerCount", container.getActiveConsumerCount());
      details.put("queueNames", container.getQueueNames());
      details.put("autoStartup", container.isAutoStartup());
      return details;
    }
    return Map.of();
  }

  @EventListener
  public void onApplicationEvent(ContextRefreshedEvent event) {
    // This runs after the application context is fully initialized
    logger.info("=== Application Started - Verifying Listener Status ===");

    listenerContainers.forEach((listenerId, container) ->
        logger.info("Listener {}: isRunning={}, isActive={}, ActiveConsumers={}",
            listenerId, container.isRunning(), container.isActive(), container.getActiveConsumerCount())
    );

    logger.info("=== Listener Verification Complete ===");
  }
}
