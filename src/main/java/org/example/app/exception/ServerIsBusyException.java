package org.example.app.exception;

public class ServerIsBusyException extends RuntimeException {

  public ServerIsBusyException(String ex) {
    super(ex);
  }
}
