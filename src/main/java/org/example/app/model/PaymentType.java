package org.example.app.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

  @JsonValue // This tells Jackson to use the method field for serialization
  public String toValue() {
    return method;
  }

  @JsonCreator // This tells Jackson to use this method for deserialization
  public static PaymentType fromValue(String value) {
    for (PaymentType type : values()) {
      if (type.method.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown payment type: " + value);
  }
}
