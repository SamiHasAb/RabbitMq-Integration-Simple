package org.example.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class HealthCheckReport {

  @JsonProperty("App health status")
  private boolean healthy;
  @JsonProperty("Health Results")
  private List<HealthCheckResult> healthCheckResults = new ArrayList<>();


  //Adding a result to the list
  public void addHealthCheckResults(HealthCheckResult healthCheckResult) {
    healthCheckResults.add(healthCheckResult);
    this.healthy = this.healthCheckResults.stream().allMatch(HealthCheckResult::isHealthy);
  }

  public boolean isHealthy() {
    return healthy;
  }

  public List<HealthCheckResult> getHealthCheckResults() {
    return healthCheckResults;
  }

  @Override
  public String toString() {
    return "HealthCheckReport{" +
        "healthy=" + healthy +
        ", healthCheckResults=" + healthCheckResults +
        '}';
  }
}
