package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;

public class ChannelNotFoundException extends DiscodeitException {
  public ChannelNotFoundException() {
    super(ErrorCode.CHANNEL_NOT_FOUND);
  }

}
