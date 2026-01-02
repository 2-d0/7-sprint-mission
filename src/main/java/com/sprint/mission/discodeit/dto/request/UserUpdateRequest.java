package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.Message;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank(message = "수정할 사용자 이름은 필수입니다.")
    String newUsername,

    @Email(message = "유효한 이메일이어야 합니다.")
    String newEmail,

    @NotBlank(message = "수정할 비밀번호는 필수입니다.")
    @Size(min = 8, message = "수정할 비밀번호는 최소 8자 이상이어야 합니다.")
    String newPassword
) {

}
