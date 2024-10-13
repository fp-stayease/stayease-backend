package com.finalproject.stayease.exceptions.properties;

public class RoomAvailabilityNotFoundException extends RuntimeException {

  public RoomAvailabilityNotFoundException(String message) {
    super(message);
  }

  public RoomAvailabilityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
