package com.sprint.mission.discodeit.exception.global;

import org.springframework.http.HttpStatus;

  public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_FOUND("메시지 ID를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    READ_STATUS_NOT_FOUND("읽음 상태를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_STATUS_NOT_FOUND("사용자 상태를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BINARY_CONTENT_NOT_FOUND("지정한 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),;


  private String message;
  private HttpStatus status;

  ErrorCode(String message, HttpStatus status) {
    this.message = message;
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
