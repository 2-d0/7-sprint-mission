package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
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
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Transactional
  @Override
  public ChannelDto create(PublicChannelCreateRequest request) {
    log.info("Public Channel 생성 요청 - name={}, description={}", request.name(), request.description());

    String name = request.name();
    String description = request.description();
    Channel channel = new Channel(ChannelType.PUBLIC, name, description);

    channelRepository.save(channel);

    log.info("Public Channel 생성 성공 - channelId={}, name={}, description={}", channel.getId(), name, description);
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest request) {
    log.info("Private Channel 생성 요청 - participantCount={}", request.participantIds().size());
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);

    List<ReadStatus> readStatuses = userRepository.findAllById(request.participantIds()).stream()
        .map(user -> new ReadStatus(user, channel, channel.getCreatedAt()))
        .toList();
    readStatusRepository.saveAll(readStatuses);

    log.info("Private Channel 생성 성공 - channelId={}, participantIds={}", channel.getId(), request.participantIds());
    return channelMapper.toDto(channel);
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    log.info("Channel 단건 조회 요청 - channelId={}", channelId);
    ChannelDto result = channelRepository.findById(channelId)
        .map(channelMapper::toDto)
        .orElseThrow(() -> {
          log.warn("Channel 단건 조회 실패 - 존재하지 않는 channelId={}", channelId);
          return new ChannelNotFoundException();
        });
    log.info("Channel 단건 조회 성공 - channelId={}", channelId);
    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {

    log.info("Channel 다건 요청 조회 - 요청된 ID={}", userId);

    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannel)
        .map(Channel::getId)
        .toList();

    log.info("Channel 다건 요청 성공 - 요청된 ID={}, 반환된 수={}", userId, mySubscribedChannelIds.size());
    return channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, mySubscribedChannelIds)
        .stream()
        .map(channelMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    log.info("Channel 수정 요청 - channelId={}, newName={}, newDescription={}",
        channelId, request.newName(), request.newDescription());
    String newName = request.newName();
    String newDescription = request.newDescription();
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("Channel 수정 실패 - 존재하지 않음 ChannelId={}", channelId);
          return new ChannelNotFoundException();
        });
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      log.warn("Channel 수정 실패 - Private channel은 수정 불가 - channelId={}", channelId);
      throw new PrivateChannelUpdateException();
    }
    channel.update(newName, newDescription);

    log.info("Channel 수정 성공 - channelId={}, newName={}, newDescription={}",
        channelId, newName, newDescription);
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public void delete(UUID channelId) {
    log.info("Channel 삭제 요청 - channelId={}", channelId);
    if (!channelRepository.existsById(channelId)) {
      log.warn("Channel 삭제 실패 - 존재하지 않는 채널 channelId={}", channelId);
      throw new ChannelNotFoundException();
    }

    messageRepository.deleteAllByChannelId(channelId);
    readStatusRepository.deleteAllByChannelId(channelId);

    log.info("Channel 삭제 성공 - channelId={}", channelId);
    channelRepository.deleteById(channelId);
  }
}
