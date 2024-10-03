package com.finalproject.stayease.exceptions.properties;

public class ConflictingRateException extends RuntimeException {

  public ConflictingRateException(String message) {
    super(message);
  }

  public ConflictingRateException(String message, Throwable cause) {
    super(message, cause);
  }

}
