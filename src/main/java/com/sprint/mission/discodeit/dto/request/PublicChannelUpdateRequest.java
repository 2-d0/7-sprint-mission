package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PublicChannelUpdateRequest(
    @NotBlank(message = "수정할 채널 이름은 필수입니다.")
    String newName,
    String newDescription
) {

}
