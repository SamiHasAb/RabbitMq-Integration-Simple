package org.example.app.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConstants {

  public static String APPLICATION_NAME;

  @Value("${spring.application.name}")
  private String appName;

  @PostConstruct
  public void init() {
    APPLICATION_NAME = appName;
  }
}
