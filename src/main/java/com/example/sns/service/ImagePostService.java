package com.example.sns.service;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.sns.domain.ImagePost;
import com.example.sns.domain.User;
import com.example.sns.dto.response.ImagePostResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.repository.ImagePostRepository;
import com.example.sns.repository.PinRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이미지 게시글 서비스.
 *
 * RULE 2.3: 트랜잭션 경계 Service 계층.
 * RULE 3.5.5: @Transactional Service 계층에만.
 * RULE 1.2: IDOR 방지 - 수정·삭제 시 소유권 검증.
 * Step 9: Multipart 업로드·파일 저장·ImagePost CRUD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImagePostService {

    private static final String MSG_IMAGE_POST_NOT_FOUND = "이미지 게시글을 찾을 수 없습니다.";

    private final ImagePostRepository imagePostRepository;
    private final FileStorageService fileStorageService;
    private final PinRepository pinRepository;

    private static final String STORAGE_SUB_DIR = "image-posts";

    /**
     * 이미지 게시글 목록 조회 (페이징·검색). 비로그인 허용.
     */
    @Transactional(readOnly = true)
    public Page<ImagePostResponse> getList(String keyword, Pageable pageable) {
        return imagePostRepository.findAllByKeyword(keyword, pageable)
                .map(ImagePostResponse::from);
    }

    /**
     * 이미지 게시글 상세 조회. 비로그인 허용.
     */
    @Transactional(readOnly = true)
    public ImagePostResponse getById(Long id) {
        ImagePost post = findById(id);
        return ImagePostResponse.from(post);
    }

    /**
     * 이미지 게시글 작성. 로그인 필수.
     */
    @Transactional
    public ImagePostResponse create(String title, String content, MultipartFile image,
                                    Double latitude, Double longitude, Long pinId, User author) {
        String storedPath = fileStorageService.storeImage(image, STORAGE_SUB_DIR);
        var pin = pinId != null ? pinRepository.findById(pinId).orElse(null) : null;

        ImagePost post = ImagePost.builder()
                .author(author)
                .title(title)
                .content(content)
                .imageStoragePath(storedPath)
                .latitude(latitude)
                .longitude(longitude)
                .pin(pin)
                .build();
        ImagePost saved = imagePostRepository.save(post);
        log.info("이미지 게시글 작성: imagePostId={}, authorId={}", saved.getId(), author.getId());
        return ImagePostResponse.from(saved);
    }

    /**
     * 이미지 게시글 수정. 작성자만. image가 있으면 교체.
     */
    @Transactional
    public ImagePostResponse update(Long id, String title, String content,
                                    MultipartFile image, User currentUser) {
        ImagePost post = findById(id);
        if (!post.isAuthor(currentUser)) {
            log.warn("이미지 게시글 수정 IDOR 시도: imagePostId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 게시글만 수정할 수 있습니다.");
        }

        String newPath = post.getImageStoragePath();
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteIfExists(post.getImageStoragePath());
            newPath = fileStorageService.storeImage(image, STORAGE_SUB_DIR);
        }
        post.update(title, content, newPath);
        return ImagePostResponse.from(post);
    }

    /**
     * 이미지 게시글 삭제. 작성자만. 저장된 파일도 삭제.
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        ImagePost post = findById(id);
        if (!post.isAuthor(currentUser)) {
            log.warn("이미지 게시글 삭제 IDOR 시도: imagePostId={}, userId={}", id, currentUser.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 게시글만 삭제할 수 있습니다.");
        }
        fileStorageService.deleteIfExists(post.getImageStoragePath());
        imagePostRepository.delete(post);
        log.info("이미지 게시글 삭제: imagePostId={}, authorId={}", id, currentUser.getId());
    }

    /**
     * 이미지 파일 리소스 반환. 상세 조회용.
     */
    @Transactional(readOnly = true)
    public Resource getImageResource(Long id) {
        ImagePost post = findById(id);
        Path path = fileStorageService.resolveStoredPath(post.getImageStoragePath());
        if (!Files.exists(path)) {
            log.warn("저장된 이미지 파일 없음: imagePostId={}", id);
            throw new BusinessException(ErrorCode.NOT_FOUND, "이미지 파일을 찾을 수 없습니다.");
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.isReadable()) {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "이미지를 읽을 수 없습니다.");
            }
            return resource;
        } catch (Exception e) {
            log.error("이미지 리소스 로드 실패: imagePostId={}", id, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "이미지를 불러올 수 없습니다.");
        }
    }

    /**
     * 작성자별 이미지 게시글 목록. 마이페이지용.
     */
    @Transactional(readOnly = true)
    public Page<ImagePostResponse> getListByAuthor(User author, Pageable pageable) {
        return imagePostRepository.findByAuthor(author, pageable)
                .map(ImagePostResponse::from);
    }

    private ImagePost findById(Long id) {
        return imagePostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, MSG_IMAGE_POST_NOT_FOUND));
    }
}
