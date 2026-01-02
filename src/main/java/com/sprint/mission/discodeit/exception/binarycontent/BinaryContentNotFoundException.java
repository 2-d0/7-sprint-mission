package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class BinaryContentNotFoundException extends DiscodeitException {
  public BinaryContentNotFoundException() {
    super(ErrorCode.BINARY_CONTENT_NOT_FOUND);
  }

}
