package com.example.sns.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sns.domain.Post;
import com.example.sns.domain.User;
import com.example.sns.dto.request.PostCreateRequest;
import com.example.sns.dto.request.PostUpdateRequest;
import com.example.sns.dto.response.PostResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.repository.PinRepository;
import com.example.sns.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시글 서비스.
 *
 * RULE 2.3: 트랜잭션 경계 Service 계층.
 * RULE 3.5.5: @Transactional Service 계층에만.
 * RULE 1.2: IDOR 방지 - 수정·삭제 시 소유권 검증.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final String MSG_POST_NOT_FOUND = "게시글을 찾을 수 없습니다.";

    private final PostRepository postRepository;
    private final PinRepository pinRepository;

    /**
     * 게시글 목록 조회 (페이징·검색). 비로그인 허용.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getList(String keyword, Pageable pageable) {
        return postRepository.findAllByKeyword(keyword, pageable)
                .map(PostResponse::from);
    }

    /**
     * 게시글 상세 조회. 비로그인 허용.
     */
    @Transactional(readOnly = true)
    public PostResponse getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, MSG_POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    /**
     * 게시글 작성. 로그인 필수.
     */
    @Transactional
    public PostResponse create(PostCreateRequest request, User author) {
        var pin = request.pinId() != null
                ? pinRepository.findById(request.pinId()).orElse(null)
                : null;
        Post post = Post.builder()
                .author(author)
                .title(request.title())
                .content(request.content())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .pin(pin)
                .build();
        Post saved = postRepository.save(post);
        log.info("게시글 작성: postId={}, authorId={}", saved.getId(), author.getId());
        return PostResponse.from(saved);
    }

    /**
     * 게시글 수정. 작성자만. 타인 글 시 403.
     */
    @Transactional
    public PostResponse update(Long id, PostUpdateRequest request, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, MSG_POST_NOT_FOUND));
        if (!post.isAuthor(currentUser)) {
            log.warn("게시글 수정 IDOR 시도: postId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 게시글만 수정할 수 있습니다.");
        }
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    /**
     * 반경(km) 내 게시글 조회. 비로그인 가능.
     * Step 11: 위도·경도가 있는 게시글만 반환.
     *
     * @param lat      중심 위도
     * @param lng      중심 경도
     * @param radiusKm 반경(km)
     * @param pageable 페이징
     * @return 반경 내 게시글 목록
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getNearby(double lat, double lng, double radiusKm, Pageable pageable) {
        log.debug("반경 내 게시글 조회: lat={}, lng={}, radiusKm={}", lat, lng, radiusKm);
        return postRepository.findWithinRadius(radiusKm, lat, lng, pageable)
                .map(PostResponse::from);
    }

    /**
     * 게시글 삭제. 작성자만. 타인 글 시 403.
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, MSG_POST_NOT_FOUND));
        if (!post.isAuthor(currentUser)) {
            log.warn("게시글 삭제 IDOR 시도: postId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 게시글만 삭제할 수 있습니다.");
        }
        postRepository.delete(post);
        log.info("게시글 삭제: postId={}, authorId={}", id, currentUser.getId());
    }
}
