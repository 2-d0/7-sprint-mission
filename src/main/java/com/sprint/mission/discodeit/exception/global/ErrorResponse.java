package com.sprint.mission.discodeit.exception.global;


public class ErrorResponse {

  private final int status;
  private final String code;
  private final String message;

  public ErrorResponse(ErrorCode errorCode) {
    this.status = errorCode.getStatus().value();
    this.code = errorCode.name();
    this.message = errorCode.getMessage();
  }

  public int getStatus() {
    return status;
  }
  public String getCode() {
    return code;
  }
  public String getMessage() {
    return message;
  }

}
