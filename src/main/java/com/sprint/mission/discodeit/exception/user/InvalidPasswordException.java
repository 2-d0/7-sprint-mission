package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class InvalidPasswordException extends DiscodeitException {
  public InvalidPasswordException() {
    super(ErrorCode.INVALID_PASSWORD);
  }

}
