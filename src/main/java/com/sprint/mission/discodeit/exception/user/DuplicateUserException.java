package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class DuplicateUserException extends DiscodeitException {
  public DuplicateUserException() {
    super(ErrorCode.DUPLICATE_USER);
  }

}
