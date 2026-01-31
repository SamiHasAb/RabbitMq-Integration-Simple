package org.example.app.controller;


import org.example.app.model.HealthCheckReport;
import org.example.app.services.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/heath")
public class HeathCheck {

  @Autowired
  private HealthCheckService healthCheckService;

  @GetMapping
  private HealthCheckReport getHealthCheck() {
    return healthCheckService.getHealthReport();
  }
}
