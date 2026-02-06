package com.example.sns.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sns.domain.Pin;
import com.example.sns.domain.User;
import com.example.sns.dto.request.PinCreateRequest;
import com.example.sns.dto.request.PinUpdateRequest;
import com.example.sns.dto.response.PinResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.repository.PinRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Pin 서비스.
 *
 * RULE 2.3: 트랜잭션 경계 Service 계층.
 * RULE 3.5.5: @Transactional Service 계층에만.
 * RULE 1.2: IDOR 방지 - 수정·삭제 시 소유권 검증.
 * Step 10: Pin CRUD, 사용자별 목록.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PinService {

    private static final String MSG_PIN_NOT_FOUND = "Pin을 찾을 수 없습니다.";

    private final PinRepository pinRepository;

    /**
     * 사용자별 Pin 목록. 로그인 필수.
     */
    @Transactional(readOnly = true)
    public Page<PinResponse> getListByOwner(User owner, Pageable pageable) {
        return pinRepository.findByOwner(owner, pageable)
                .map(PinResponse::from);
    }

    /**
     * Pin 상세. 소유자만 조회 가능.
     */
    @Transactional(readOnly = true)
    public PinResponse getById(Long id, User currentUser) {
        Pin pin = findById(id);
        if (!pin.isOwner(currentUser)) {
            log.warn("Pin 상세 IDOR 시도: pinId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 Pin만 조회할 수 있습니다.");
        }
        return PinResponse.from(pin);
    }

    /**
     * Pin 생성. 로그인 필수.
     */
    @Transactional
    public PinResponse create(PinCreateRequest request, User owner) {
        Pin pin = Pin.builder()
                .owner(owner)
                .description(request.description())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        Pin saved = pinRepository.save(pin);
        log.info("Pin 생성: pinId={}, ownerId={}", saved.getId(), owner.getId());
        return PinResponse.from(saved);
    }

    /**
     * Pin 수정. 소유자만.
     */
    @Transactional
    public PinResponse update(Long id, PinUpdateRequest request, User currentUser) {
        Pin pin = findById(id);
        if (!pin.isOwner(currentUser)) {
            log.warn("Pin 수정 IDOR 시도: pinId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 Pin만 수정할 수 있습니다.");
        }
        pin.update(
                request.description(),
                request.latitude(),
                request.longitude()
        );
        return PinResponse.from(pin);
    }

    /**
     * Pin 삭제. 소유자만.
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        Pin pin = findById(id);
        if (!pin.isOwner(currentUser)) {
            log.warn("Pin 삭제 IDOR 시도: pinId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 Pin만 삭제할 수 있습니다.");
        }
        pinRepository.delete(pin);
        log.info("Pin 삭제: pinId={}, ownerId={}", id, currentUser.getId());
    }

    /**
     * Pin 조회 (내부용). 소유권 검증 없음.
     */
    public Pin findPinById(Long id) {
        return findById(id);
    }

    private Pin findById(Long id) {
        return pinRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, MSG_PIN_NOT_FOUND));
    }
}
