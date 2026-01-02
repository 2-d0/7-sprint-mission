package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

    @NotEmpty(message = "비공개 채널 참여자는 최소 1 명 이상이어야 합니다.")
    List<UUID> participantIds
) {

}
