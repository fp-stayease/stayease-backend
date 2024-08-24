package com.finalproject.stayease.exceptions;

public class TokenDoesNotExistException extends RuntimeException {

  public TokenDoesNotExistException(String message) {
    super(message);
  }

  public TokenDoesNotExistException(String message, Throwable cause) {
    super(message, cause);
  }
}
