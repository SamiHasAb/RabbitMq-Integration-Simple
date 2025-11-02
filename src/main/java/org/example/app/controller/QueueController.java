package org.example.app.controller;

import java.util.List;
import java.util.Map;
import org.example.app.config.RabbitMQProperties.QueueConfig;
import org.example.app.services.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queue")
public class QueueController {

  @Autowired
  private QueueService queueService;

  @GetMapping()
  public ResponseEntity<?> getAllQueue() {
    List<String> allQueue = queueService.getAllQueue();
    return ResponseEntity.ok(allQueue);
  }

  @GetMapping("/config")
  public ResponseEntity<?> getQueueConfig() {
    Map<String, QueueConfig> queuesConfig = queueService.getQueuesConfig();
    return ResponseEntity.ok(queuesConfig);
  }

  @GetMapping("/{queueName}/count")
  public ResponseEntity<?> countMessagesOnQueue(@PathVariable String queueName) {
    String messageCount = queueService.countMessages(queueName);
    return ResponseEntity.ok(messageCount);
  }

  @GetMapping("/{queueName}/peak")
  public ResponseEntity<?> peakOnQueue(@PathVariable String queueName) {
    String message = queueService.peakOnQueue(queueName);
    return ResponseEntity.ok(message);
  }

  @GetMapping()
  public ResponseEntity<?> moveMessage(@PathVariable String queueName, @RequestParam(defaultValue = "1") String number){
    queueService.moveMessage( queueName, number);
  }
}
