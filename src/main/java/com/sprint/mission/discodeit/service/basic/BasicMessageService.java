package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  //
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;
  private final PageResponseMapper pageResponseMapper;

  @Transactional
  @Override
  public MessageDto create(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    log.info("Message 생성 요청 - messageCreateRequest={}", messageCreateRequest);
    UUID channelId = messageCreateRequest.channelId();
    UUID authorId = messageCreateRequest.authorId();

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("Message 생성 실패 - Channel 존재하지 않음 channelId={}", channelId);
           return new NoSuchElementException("Channel with id " + channelId + " does not exist");
        });
    User author = userRepository.findById(authorId)
        .orElseThrow(() -> {
              log.warn("Message 생성 실패 - Author 존재하지 않음 authorId={}", authorId);
              return new NoSuchElementException("Author with id " + authorId + " does not exist");
        });

    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
        .map(attachmentRequest -> {
          String fileName = attachmentRequest.fileName();
          String contentType = attachmentRequest.contentType();
          byte[] bytes = attachmentRequest.bytes();

          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);
          binaryContentStorage.put(binaryContent.getId(), bytes);
          return binaryContent;
        })
        .toList();

    String content = messageCreateRequest.content();
    Message message = new Message(
        content,
        channel,
        author,
        attachments
    );

    messageRepository.save(message);

    log.info("Message 생성 성공 - messageId={}, channelId={}, authorId={}, attachmentCount={}",
        message.getId(), channelId, authorId, attachments.size());
    return messageMapper.toDto(message);
  }

  @Transactional(readOnly = true)
  @Override
  public MessageDto find(UUID messageId) {
    log.info("Message 조회 요청 - messageId={}", messageId);
    return messageRepository.findById(messageId)
        .map(message -> {
          MessageDto dto = messageMapper.toDto(message);
          log.info("Message 조회 성공 - messageId={}, channelId={}, authorId={}",
              message.getId(), message.getChannel().getId(), message.getAuthor().getId());
          return dto;
        })
        .orElseThrow(() -> {
          log.warn("Message 조회 실패 - messageId를 찾을 수 없음 messageId={}", messageId);
          return new NoSuchElementException("Message with id " + messageId + " not found");
        });
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant createAt,
      Pageable pageable) {
    log.info("Message 목록 조회 요청 - channelId={}, cursor={}, pageSize={}", channelId, createAt, pageable.getPageSize());
    Slice<MessageDto> slice = messageRepository.findAllByChannelIdWithAuthor(channelId,
            Optional.ofNullable(createAt).orElse(Instant.now()),
            pageable)
        .map(messageMapper::toDto);

    Instant nextCursor = null;
    if (!slice.getContent().isEmpty()) {
      nextCursor = slice.getContent().get(slice.getContent().size() - 1)
          .createdAt();
    }

    log.info("Message 목록 조회 성공 - channelId={}, resultCount={}, hasNext={}, nextCursor={}",
        channelId, slice.getContent().size(), slice.hasNext(), nextCursor);

    return pageResponseMapper.fromSlice(slice, nextCursor);
  }

  @Transactional
  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    log.info("Message 수정 요청 - messageId={}, newContent={}", messageId, request.newContent());
    String newContent = request.newContent();
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("Message 수정 실패 - 존재하지 않는 messageId={}", messageId);
          return new NoSuchElementException("Message with id " + messageId + " not found");
          });
    message.update(newContent);
    log.info("Message 수정 성공 - messageId={}, newContent={}", messageId, newContent);
    return messageMapper.toDto(message);
  }


  @Transactional
  @Override
  public void delete(UUID messageId) {
    log.info("Message 삭제 요청 - messageId={}", messageId);
    if (!messageRepository.existsById(messageId)) {
      log.warn("Message 삭제 실패 - 찾을 수 없는 messageId={}", messageId);
      throw new NoSuchElementException("Message with id " + messageId + " not found");
    }

    messageRepository.deleteById(messageId);
    log.info("Message 삭제 성공 - messageId={}", messageId);
  }
}
