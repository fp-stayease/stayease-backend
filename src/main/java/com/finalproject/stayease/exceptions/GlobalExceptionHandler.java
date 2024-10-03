package com.finalproject.stayease.exceptions;

import com.finalproject.stayease.exceptions.auth.InvalidCredentialsException;
import com.finalproject.stayease.exceptions.auth.InvalidRefreshTokenException;
import com.finalproject.stayease.exceptions.auth.PasswordDoesNotMatchException;
import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.exceptions.properties.DuplicateCategoryException;
import com.finalproject.stayease.exceptions.properties.DuplicatePropertyException;
import com.finalproject.stayease.exceptions.properties.DuplicateRoomException;
import com.finalproject.stayease.exceptions.properties.PeakSeasonRateNotFoundException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.properties.RoomNotFoundException;
import com.finalproject.stayease.exceptions.properties.RoomUnavailableException;
import com.finalproject.stayease.exceptions.users.NoLinkedSocialLoginException;
import com.finalproject.stayease.exceptions.users.TenantInfoNotFoundException;
import com.finalproject.stayease.exceptions.users.UserNotFoundException;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.exceptions.utils.DuplicateEntryException;
import com.finalproject.stayease.exceptions.utils.ImageRetrievalException;
import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.exceptions.utils.InvalidTokenException;
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
    EXCEPTION_STATUS_MAP.put(CategoryNotFoundException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(DataNotFoundException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(DuplicateCategoryException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(DuplicateEntryException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(DuplicatePropertyException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(DuplicateRoomException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(ImageRetrievalException.class, HttpStatus.INTERNAL_SERVER_ERROR);
    EXCEPTION_STATUS_MAP.put(InvalidCredentialsException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(InvalidDateException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(InvalidRefreshTokenException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(InvalidRequestException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(InvalidTokenException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(NoLinkedSocialLoginException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(PeakSeasonRateNotFoundException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(PasswordDoesNotMatchException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(PropertyNotFoundException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(RoomNotFoundException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(RoomUnavailableException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_STATUS_MAP.put(TenantInfoNotFoundException.class, HttpStatus.NOT_FOUND);
    EXCEPTION_STATUS_MAP.put(UnauthorizedOperationsException.class, HttpStatus.UNAUTHORIZED);
    EXCEPTION_STATUS_MAP.put(UserNotFoundException.class, HttpStatus.NOT_FOUND);
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
