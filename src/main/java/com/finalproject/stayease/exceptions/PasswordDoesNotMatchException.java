package com.finalproject.stayease.exceptions;

public class PasswordDoesNotMatchException extends RuntimeException {

  public PasswordDoesNotMatchException(String message) {
    super(message);
  }

  public PasswordDoesNotMatchException(String message, Throwable cause) {
    super(message, cause);
  }
}
