package org.example.app.services;

import org.example.app.model.HealthCheckReport;
import org.example.app.model.HealthCheckResult;
import org.example.app.model.ListenerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

  @Autowired
  private RabbitListenerService rabbitListenerService;

  public HealthCheckReport getHealthReport() {

    HealthCheckReport healthCheckReport = new HealthCheckReport();

    for (ListenerInfo listenersStatus : rabbitListenerService.getAllListenersStatus()) {
      HealthCheckResult healthCheckResult = this.preformHealthCheck(listenersStatus);
      healthCheckReport.addHealthCheckResults(healthCheckResult);
    }

    // Can add more health preform methods here and add the result to healthCheckReport

    return healthCheckReport;
  }

  private HealthCheckResult preformHealthCheck(ListenerInfo listenersStatus) {
    return new HealthCheckResult(
        listenersStatus.isRunning(),
        listenersStatus.getListenerId(),
        listenersStatus.isRunning() ? "" : "Listener is not running");
  }
}
