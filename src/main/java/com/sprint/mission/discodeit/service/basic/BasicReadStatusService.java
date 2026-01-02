package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Transactional
  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    UUID userId = request.userId();
    UUID channelId = request.channelId();

    log.info("ReadStatus 생성 요청 - userId={}, channelId={}", request.userId(), request.channelId());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("ReadStatus 생성 실패 - 존재하지 않는 userId={}", userId);
          return new UserNotFoundException();
          });
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("ReadStatus 생성 실패 - 존재하지 않는 channelId={}", channelId);
          return new ChannelNotFoundException();
        });

    if (readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())) {
      log.warn("ReadStatus 생성 실패 - 이미 존재하는 userId={}, channelId={}", userId, channelId);
      throw new DuplicateUserException();
    }

    Instant lastReadAt = request.lastReadAt();
    ReadStatus readStatus = new ReadStatus(user, channel, lastReadAt);
    readStatusRepository.save(readStatus);

    log.info("ReadStatus 생성 성공 - userId={}, channelId={}", userId, channelId);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto find(UUID readStatusId) {
    log.info("ReadStatus 조회 요청 - readStatusId={}", readStatusId);

    return readStatusRepository.findById(readStatusId)
        .map(readStatus -> {
          ReadStatusDto dto = readStatusMapper.toDto(readStatus);
          log.info("ReadStatus 조회 성공 - readStatusId={}, userId={}, channelId={}",
              dto.id(), dto.userId(), dto.channelId());
          return dto;
        })
        .orElseThrow(() -> {
          log.info("ReadStatus 조회 실패 - 존재하지 않는 readStatusId={}", readStatusId);
          return new ReadStatusNotFoundException();
        });
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    log.info("ReadStatus 조회 요청 - userId={}", userId);
    return readStatusRepository.findAllByUserId(userId).stream()
        .map(readStatus -> {
          ReadStatusDto dto = readStatusMapper.toDto(readStatus);
          log.info("ReadStatus 조회 성공 - userId={}, readStatus={}", dto.id(), dto);
          return dto;
        })
        .toList();
  }

  @Transactional
  @Override
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    log.info("ReadStatus 수정 요청 - readStatusId={}", readStatusId);
    Instant newLastReadAt = request.newLastReadAt();
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> {
          log.warn("ReadStatus 수정 실패 - 찾을 수 없는 readStatusId={}", readStatusId);
          return new ReadStatusNotFoundException();
            });
    readStatus.update(newLastReadAt);
    log.info("ResaStatus 수정 성공 - readStatusId={}", readStatusId);
    return readStatusMapper.toDto(readStatus);
  }

  @Transactional
  @Override
  public void delete(UUID readStatusId) {
    log.info("ReadStatus 삭제 요청 - readStatusId={}", readStatusId);
    if (!readStatusRepository.existsById(readStatusId)) {
      log.warn("ReadStatus 삭제 실패 - 찾을 수 없는 readStatusId={}", readStatusId);
      throw new ReadStatusNotFoundException();
    }
    readStatusRepository.deleteById(readStatusId);
    log.info("ReadStatus 삭제 성공 - readStatusId={}", readStatusId);
  }
}
