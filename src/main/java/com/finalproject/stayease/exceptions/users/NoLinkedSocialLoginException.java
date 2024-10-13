package com.finalproject.stayease.exceptions.users;

public class NoLinkedSocialLoginException extends RuntimeException {

  public NoLinkedSocialLoginException(String message) {
    super(message);
  }

  public NoLinkedSocialLoginException(String message, Throwable cause) {
    super(message, cause);
  }
}
