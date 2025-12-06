package org.example.app.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ListenerInfo {


  private final String listenerId;
  private final boolean isRunning;
  private final LocalDateTime startedAt;
  private final String queueName;
  private final LocalDateTime lastChecked;
  private final int activeConsumerCount;
  private final String[] queueNames;

  // Constructor for basic info - FIXED parameter order
  public ListenerInfo(String queueName, String listenerId, boolean isRunning, LocalDateTime startedAt) {
    this.listenerId = listenerId;
    this.isRunning = isRunning;
    this.startedAt = startedAt;
    this.queueName = queueName;
    this.lastChecked = LocalDateTime.now();
    this.activeConsumerCount = 0;
    this.queueNames = new String[]{queueName};
  }

  // Full constructor - FIXED parameter order to match usage
  public ListenerInfo(String listenerId, boolean isRunning,
      LocalDateTime startedAt, String queueName,
      int activeConsumerCount, String[] queueNames) {
    this.listenerId = listenerId;
    this.isRunning = isRunning;
    this.startedAt = startedAt;
    this.queueName = queueName;
    this.lastChecked = LocalDateTime.now();
    this.activeConsumerCount = activeConsumerCount;
    this.queueNames = queueNames;
  }

  // Getters
  public String getListenerId() {
    return listenerId;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public String getQueueName() {
    return queueName;
  }

  public LocalDateTime getLastChecked() {
    return lastChecked;
  }

  public int getActiveConsumerCount() {
    return activeConsumerCount;
  }

  public String[] getQueueNames() {
    return queueNames;
  }
}
