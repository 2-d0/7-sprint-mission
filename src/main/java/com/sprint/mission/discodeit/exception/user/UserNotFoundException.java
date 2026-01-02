package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class UserNotFoundException extends DiscodeitException {
  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

}
