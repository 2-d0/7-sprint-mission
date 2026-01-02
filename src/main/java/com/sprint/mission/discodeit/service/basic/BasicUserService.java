package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    String username = userCreateRequest.username();
    String email = userCreateRequest.email();

    log.info("User 생성 요청 - username={}, email={}", username, email);

    if (userRepository.existsByEmail(email)) {
      log.warn("User 생성 실패 - 이미 존재하는 email={}", email);
      throw new DuplicateUserException();
    }
    if (userRepository.existsByUsername(username)) {
      log.warn("이미 존재하는 username={}", username);
      throw new DuplicateUserException();
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
        .map(profileRequest -> {
          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();
          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);
          binaryContentStorage.put(binaryContent.getId(), bytes);
          log.info("User 프로필 이미지 저장 성공 - BinaryContentId={}, fileName={}, size={}bytes",
              binaryContent.getId(), fileName, bytes.length);
          return binaryContent;
        })
        .orElse(null);
    String password = userCreateRequest.password();

    User user = new User(username, email, password, nullableProfile);
    Instant now = Instant.now();
    UserStatus userStatus = new UserStatus(user, now);

    userRepository.save(user);
    log.info("User 생성 성공 - userId={}, username={}", user.getId(), username);
    return userMapper.toDto(user);
  }

  @Override
  public UserDto find(UUID userId) {
    log.info("User 단건 조회 요청 - userId={}", userId);
    return userRepository.findById(userId)
        //.map(userMapper::toDto)
        .map(user -> {
          UserDto dto= userMapper.toDto(user);
          log.info("User 단건 조회 성공 - userId={}, username={}", user.getId(), user.getUsername());
          return dto;
        })
        .orElseThrow(() -> {
          log.warn("User 단건 조회 실패 - 찾을 수 없는 userId={}", userId);
          return new UserNotFoundException();
        });
  }

  @Override
  public List<UserDto> findAll() {
    log.info("User 다건 조회 요청");

    List<User> users = userRepository.findAllWithProfileAndStatus();
    log.info("User 다건 조회 성공 - 총 {}건", users.size());

    return userRepository.findAllWithProfileAndStatus()
        .stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("User 수정 실패 - 찾을 수 없는 userId={}", userId);
          return new UserNotFoundException();
        });

    String newUsername = userUpdateRequest.newUsername();
    String newEmail = userUpdateRequest.newEmail();
    log.info("User 수정 요청 - userId={}, newUsername={}, newEmail={}",
        userId, userUpdateRequest.newUsername(), userUpdateRequest.newEmail());

    if (userRepository.existsByEmail(newEmail)) {
      log.warn("User 수정 실패 - 이미 존재하는 email={}", newEmail);
      throw new DuplicateUserException();
    }
    if (userRepository.existsByUsername(newUsername)) {
      log.warn("User 수정 실패 - 이미 존재하는 username={}", newUsername);
      throw new DuplicateUserException();
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
        .map(profileRequest -> {

          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();
          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);
          binaryContentStorage.put(binaryContent.getId(), bytes);
          return binaryContent;
        })
        .orElse(null);

    String newPassword = userUpdateRequest.newPassword();
    user.update(newUsername, newEmail, newPassword, nullableProfile);
    log.info("User 수정 성공 - userId={}", userId);
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void delete(UUID userId) {
    log.info("User 삭제 요청 - userId={}", userId);
    if (userRepository.existsById(userId)) {
      log.warn("User 삭제 실패 - 찾을 수 없는 userId={}", userId);
      throw new UserNotFoundException();
    }

    log.info("User 삭제 성공 - userId={}", userId);
    userRepository.deleteById(userId);
  }
}
