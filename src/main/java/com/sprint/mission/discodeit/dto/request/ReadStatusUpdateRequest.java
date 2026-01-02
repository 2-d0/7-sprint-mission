package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReadStatusUpdateRequest(
    @NotNull(message = "수정할 읽은 시각은 필수입니다.")
    Instant newLastReadAt
) {

}
