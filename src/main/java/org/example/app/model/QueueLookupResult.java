package org.example.app.model;

import java.util.Map.Entry;
import org.example.app.config.RabbitMQProperties.QueueConfig;

public class QueueLookupResult {

  public enum QueueType { DEFAULT, DLQ }

  private final Entry<String, QueueConfig> entry;
  private final QueueType type;

  public QueueLookupResult(Entry<String, QueueConfig> entry, QueueType type) {
    this.entry = entry;
    this.type = type;
  }

  public Entry<String, QueueConfig> getEntry() { return entry; }
  public QueueType getType() { return type; }
}
