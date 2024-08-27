package com.finalproject.stayease.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {

  public InvalidRefreshTokenException(String message) {
    super(message);
  }

  public InvalidRefreshTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
