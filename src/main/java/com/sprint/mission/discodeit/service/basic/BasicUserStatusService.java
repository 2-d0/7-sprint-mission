package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;

  @Transactional
  @Override
  public UserStatusDto create(UserStatusCreateRequest request) {
    log.info("UserStatus 생성 요청 - userId={}", request.userId());
    UUID userId = request.userId();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("UserStatus 생성 실패 - 찾을 수 없는 userId={}", userId);
          return new NoSuchElementException("User with id " + userId + " not found");
        });
    Optional.ofNullable(user.getStatus())
        .ifPresent(status -> {
          log.warn("UserStatus 생성 실패 - 이미 존재하는 userId={}", userId);
          throw new IllegalArgumentException("UserStatus with id " + userId + " already exists");
        });

    Instant lastActiveAt = request.lastActiveAt();
    UserStatus userStatus = new UserStatus(user, lastActiveAt);
    userStatusRepository.save(userStatus);
    log.info("UserStatus 생성 성공 - userId={}", userId);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto find(UUID userStatusId) {
    log.info("UserStatus 단건 조회 요청 - userStatusId={}", userStatusId);
    return userStatusRepository.findById(userStatusId)
        .map(userStatus -> {
            UserStatusDto dto = userStatusMapper.toDto(userStatus);
            log.info("UserStatus 단건 조회 성공 - userId={}, userStatusId={}", dto.id(), dto.userId());
            return dto;
        })
        .orElseThrow(() -> {
              log.warn("UserStatus 단건 조회 실패 - 찾을 수 없는 userStatusId={}", userStatusId);
              return new NoSuchElementException("UserStatus with id " + userStatusId + " not found");
            });
  }

  @Override
  public List<UserStatusDto> findAll() {
    log.info("UserStatus 다건 조회 요청");

    List<UserStatus> userStatusList = userStatusRepository.findAll();
    log.info("UserStatus 다건 조회 성공 - 총 {}건", userStatusList.size());

    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request) {
    Instant newLastActiveAt = request.newLastActiveAt();

    UserStatus userStatus = userStatusRepository.findById(userStatusId)
        .orElseThrow(() -> {
              log.warn("UserStatus 수정 실패 - 찾을 수 없는 userStatusId={}", userStatusId);
              return new NoSuchElementException("UserStatus with id " + userStatusId + " not found");
            });
    userStatus.update(newLastActiveAt);
    log.info("UserStatus 수정 성공 - userStatusId={}", userStatusId);
    return userStatusMapper.toDto(userStatus);
  }

  @Transactional
  @Override
  public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    log.info("UserStatus 수정 요청 - userId={}", userId);
    Instant newLastActiveAt = request.newLastActiveAt();

    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> {
          log.warn("UserStatus 수정 실패 - 찾을 수 없는 userId={}", userId);
          return new NoSuchElementException("UserStatus with userId " + userId + " not found");
        });
    userStatus.update(newLastActiveAt);

    log.info("UserStatus 수정 성공 - userId={}", userId);
    return userStatusMapper.toDto(userStatus);
  }

  @Transactional
  @Override
  public void delete(UUID userStatusId) {
    log.info("UserStatus 삭제 요청 - userStatusId={}", userStatusId);
    if (!userStatusRepository.existsById(userStatusId)) {
      log.warn("UserStatus 삭제 실패 - 찾을 수 없는 userStatusId={}", userStatusId);
      throw new NoSuchElementException("UserStatus with id " + userStatusId + " not found");
    }
    log.info("UserStatus 삭제 성공 - userStatusId={}", userStatusId);
    userStatusRepository.deleteById(userStatusId);
  }
}
