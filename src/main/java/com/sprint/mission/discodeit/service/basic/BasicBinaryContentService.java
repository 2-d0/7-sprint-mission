package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request) {

    String fileName = request.fileName();
    byte[] bytes = request.bytes();
    String contentType = request.contentType();

    log.info("BinaryContent 생성 요청 - fileName={}, contentType={}, size={}bytes",
        fileName, contentType, bytes.length);

    BinaryContent binaryContent = new BinaryContent(
        fileName,
        (long) bytes.length,
        contentType
    );
    binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(binaryContent.getId(), bytes);

    log.info("BinaryContent 생성 성공 - binaryContentId={}", binaryContent.getId());

    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public BinaryContentDto find(UUID binaryContentId) {

    log.info("BinaryContent 단건 조회 요청 - binaryContentId={}", binaryContentId);

    BinaryContentDto binaryContentDto =
        binaryContentRepository.findById(binaryContentId)
            .map(binaryContentMapper::toDto)
            .orElseThrow(() -> {
              log.warn("BinaryContent 단건 조회 실패 - 존재하지 않음 binaryContentId={}", binaryContentId);
              return new NoSuchElementException(
                  "BinaryContent with id " + binaryContentId + " not found");
            });

    log.info("BinaryContent 단건 조회 성공 - BinaryContentId={}", binaryContentId);
    return binaryContentDto;
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {

    log.info("BinaryContent 다건 조회 요청 - 요청된 ID 수={}", binaryContentIds.size());

    List<BinaryContentDto> result = binaryContentRepository.findAllById(binaryContentIds).stream()
        .map(binaryContentMapper::toDto)
        .toList();

    log.info("BinaryContent 다건 요청 성공 - 반환된 수={}", binaryContentIds.size());
    return result;
  }

  @Transactional
  @Override
  public void delete(UUID binaryContentId) {

    log.info("BinaryContent 삭제 요청 - binaryContentId={}", binaryContentId);
    if (!binaryContentRepository.existsById(binaryContentId)) {
      log.warn("BinaryContent 삭제 실패 - binaryContentId={}", binaryContentId);
      throw new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found");
    }

    binaryContentRepository.deleteById(binaryContentId);
    log.info("BinaryContent 삭제 성공 - binaryContentId={}", binaryContentId);
  }
}
