package com.finalproject.stayease.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_STATUS_MAP = new HashMap<>();

  static {
    EXCEPTION_STATUS_MAP.put(DuplicateEntryException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(DataNotFoundException.class, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    HttpStatus status = EXCEPTION_STATUS_MAP.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorResponse errorResponse = new ErrorResponse(
      LocalDateTime.now(),
      status.value(),
      ex.getClass().getSimpleName(),
      ex.getMessage(),
      ex.getStackTrace()[0].toString()
    );
    log.error("Exception: ", ex);
    return new ResponseEntity<>(errorResponse, status);
  }

  @Data
  public static class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String trace;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String trace) {
      this.timestamp = timestamp;
      this.status = status;
      this.error = error;
      this.message = message;
      this.trace = trace;
    }
  }
}
