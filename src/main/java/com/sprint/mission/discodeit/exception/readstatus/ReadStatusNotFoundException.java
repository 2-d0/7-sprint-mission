package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class ReadStatusNotFoundException extends DiscodeitException {
  public ReadStatusNotFoundException() {
    super(ErrorCode.READ_STATUS_NOT_FOUND);
  }

}
