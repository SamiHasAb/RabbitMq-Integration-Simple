package org.example.app.exception;

public class InvalidPaymentException extends RuntimeException {

  public InvalidPaymentException(String ex) {
    super(ex);
  }
}
