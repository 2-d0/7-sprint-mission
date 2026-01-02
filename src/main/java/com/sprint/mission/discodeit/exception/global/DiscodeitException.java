package com.sprint.mission.discodeit.exception.global;

import org.springframework.http.HttpStatus;

public abstract class DiscodeitException extends RuntimeException {

  private final ErrorCode errorCode;
  private final String detailMessage;
  public DiscodeitException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.detailMessage = errorCode.getMessage();
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public HttpStatus getHttpStatus() {
    return errorCode.getStatus();
  }

  public String getDetailMessage() {return detailMessage;}
}
