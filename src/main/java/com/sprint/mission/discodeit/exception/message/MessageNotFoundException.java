package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class MessageNotFoundException extends DiscodeitException {
  public MessageNotFoundException() {
    super(ErrorCode.MESSAGE_NOT_FOUND);
  }

}
