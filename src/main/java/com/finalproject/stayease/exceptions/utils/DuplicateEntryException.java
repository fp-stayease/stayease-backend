package com.finalproject.stayease.exceptions.utils;

public class DuplicateEntryException extends RuntimeException {

  public DuplicateEntryException(String message) {
    super(message);
  }

  public DuplicateEntryException(String message, Throwable cause) {
    super(message, cause);
  }
}
