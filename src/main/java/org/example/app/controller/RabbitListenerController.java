package org.example.app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.app.model.ListenerInfo;
import org.example.app.services.RabbitListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rabbitmq/listeners")
public class RabbitListenerController {


  private final RabbitListenerService rabbitListenerService;

  @Autowired
  public RabbitListenerController(RabbitListenerService rabbitListenerService) {
    this.rabbitListenerService = rabbitListenerService;
  }

  @PostMapping("/{listenerId}/stop")
  public ResponseEntity<Map<String, Object>> stopListener(@PathVariable String listenerId) {
    try {
      rabbitListenerService.stopListener(listenerId);
      ListenerInfo status = rabbitListenerService.getListenerStatus(listenerId);

      Map<String, Object> response = createSuccessResponse(
          "Listener '" + listenerId + "' stopped gracefully",
          listenerId
      );
      response.put("isRunning", status != null && status.isRunning());
      response.put("note", "Current message processing may complete");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to stop listener: " + e.getMessage()));
    }
  }

  /**
   * Force stop - cancels current message processing
   */
  @PostMapping("/{listenerId}/force-stop")
  public ResponseEntity<Map<String, Object>> forceStopListener(@PathVariable String listenerId) {
    try {
      rabbitListenerService.forceStopListener(listenerId);
      ListenerInfo status = rabbitListenerService.getListenerStatus(listenerId);

      Map<String, Object> response = createSuccessResponse(
          "Listener '" + listenerId + "' force stopped",
          listenerId
      );
      response.put("isRunning", status != null && status.isRunning());
      response.put("note", "Current message was interrupted and requeued");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to force stop listener: " + e.getMessage()));
    }
  }

  /**
   * Start a specific listener
   */
  @PostMapping("/{listenerId}/start")
  public ResponseEntity<Map<String, Object>> startListener(@PathVariable String listenerId) {
    try {
      rabbitListenerService.startListener(listenerId);
      ListenerInfo status = rabbitListenerService.getListenerStatus(listenerId);

      Map<String, Object> response = createSuccessResponse(
          "Listener '" + listenerId + "' started successfully",
          listenerId
      );
      response.put("isRunning", status != null && status.isRunning());
      response.put("startedAt", status != null ? status.getStartedAt() : null);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to start listener: " + e.getMessage()));
    }
  }

  /**
   * Get status of a specific listener
   */
  @GetMapping("/{listenerId}/status")
  public ResponseEntity<Map<String, Object>> getListenerStatus(@PathVariable String listenerId) {
    try {
      ListenerInfo info = rabbitListenerService.getListenerStatus(listenerId);

      if (info == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(createErrorResponse("Listener '" + listenerId + "' not found"));
      }

      Map<String, Object> response = new HashMap<>();
      response.put("listenerId", info.getListenerId());
      response.put("isRunning", info.isRunning());
      response.put("queueName", info.getQueueName());
      response.put("startedAt", info.getStartedAt());
      response.put("lastChecked", info.getLastChecked());
      response.put("activeConsumerCount", info.getActiveConsumerCount());
      response.put("queueNames", info.getQueueNames());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to get listener status: " + e.getMessage()));
    }
  }

  /**
   * Get status of all listeners
   */
  @GetMapping("/status")
  public ResponseEntity<Map<String, Object>> getAllListenersStatus() {
    try {
      List<ListenerInfo> allListeners = rabbitListenerService.getAllListenersStatus();

      Map<String, Object> response = new HashMap<>();
      response.put("totalListeners", allListeners.size());
      response.put("listeners", allListeners.stream()
          .map(info -> {
            Map<String, Object> listenerMap = new HashMap<>();
            listenerMap.put("listenerId", info.getListenerId());
            listenerMap.put("isRunning", info.isRunning());
            listenerMap.put("queueName", info.getQueueName());
            listenerMap.put("startedAt", info.getStartedAt());
            listenerMap.put("lastChecked", info.getLastChecked());
            listenerMap.put("activeConsumerCount", info.getActiveConsumerCount());
            return listenerMap;
          })
          .collect(Collectors.toList()));
      response.put("timestamp", LocalDateTime.now());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to get listeners status: " + e.getMessage()));
    }
  }

  /**
   * Toggle listener state (start/stop)
   */
  @PostMapping("/{listenerId}/toggle")
  public ResponseEntity<Map<String, Object>> toggleListener(@PathVariable String listenerId) {
    try {
      ListenerInfo info = rabbitListenerService.getListenerStatus(listenerId);

      if (info == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(createErrorResponse("Listener '" + listenerId + "' not found"));
      }

      if (info.isRunning()) {
        rabbitListenerService.stopListener(listenerId);
        return ResponseEntity.ok(createSuccessResponse(
            "Listener '" + listenerId + "' stopped successfully", listenerId));
      } else {
        rabbitListenerService.startListener(listenerId);
        return ResponseEntity.ok(createSuccessResponse(
            "Listener '" + listenerId + "' started successfully", listenerId));
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to toggle listener: " + e.getMessage()));
    }
  }

  /**
   * Get detailed container information
   */
  @GetMapping("/{listenerId}/debug")
  public ResponseEntity<Map<String, Object>> getContainerDebugInfo(@PathVariable String listenerId) {
    try {
      Map<String, Object> containerDetails = rabbitListenerService.getContainerDetails(listenerId);
      ListenerInfo listenerInfo = rabbitListenerService.getListenerStatus(listenerId);

      if (containerDetails == null || containerDetails.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(createErrorResponse("Listener '" + listenerId + "' not found"));
      }

      Map<String, Object> response = new HashMap<>();
      response.put("container", containerDetails);
      response.put("listenerInfo", listenerInfo);
      response.put("timestamp", LocalDateTime.now());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to get debug info: " + e.getMessage()));
    }
  }

  private Map<String, Object> createSuccessResponse(String message, String listenerId) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", message);
    response.put("listenerId", listenerId);
    response.put("timestamp", LocalDateTime.now());
    return response;
  }

  private Map<String, Object> createErrorResponse(String error) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("error", error);
    response.put("timestamp", LocalDateTime.now());
    return response;
  }

}
