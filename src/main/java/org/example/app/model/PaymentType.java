package org.example.app.model;

public enum PaymentType {
  CASH("CASH"),
  CARD("CARD"),
  SWISH("SWISH");

  private final String method;

  PaymentType(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }
}
