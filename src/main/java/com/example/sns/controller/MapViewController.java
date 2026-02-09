package com.example.sns.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sns.domain.User;
import com.example.sns.dto.request.PostCreateRequest;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.config.MapProperties;
import com.example.sns.service.AuthService;
import com.example.sns.service.ImagePostService;
import com.example.sns.service.PinService;
import com.example.sns.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 지도 관련 웹 페이지 컨트롤러.
 *
 * Step 12: Pin 클릭 시 게시글 목록·상세 페이지.
 * Step 13: 게시글 작성 폼, 위치/Pin 선택, 상세 지도 표시.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class MapViewController {

    private final PostService postService;
    private final ImagePostService imagePostService;
    private final PinService pinService;
    private final AuthService authService;
    private final MapProperties mapProperties;

    /**
     * Pin에 연결된 게시글·이미지 게시글 목록. Step 12: Pin 클릭 시 이동.
     */
    @GetMapping("/pins/{id}/posts")
    public String pinPosts(@PathVariable Long id, Model model) {
        Pageable pageable = PageRequest.of(0, 20);
        model.addAttribute("pinId", id);
        model.addAttribute("posts", postService.getByPinId(id, pageable));
        model.addAttribute("imagePosts", imagePostService.getByPinId(id, pageable));
        return "pin-posts";
    }

    /**
     * 게시글 상세 (웹 뷰). Step 12: Pin→게시글 목록→상세 이동.
     */
    @GetMapping("/posts/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.getById(id));
        addMapScriptUrl(model);
        return "post-detail";
    }

    /**
     * 이미지 게시글 상세 (웹 뷰). Step 12.
     */
    @GetMapping("/image-posts/{id}")
    public String imagePostDetail(@PathVariable Long id, Model model) {
        model.addAttribute("post", imagePostService.getById(id));
        addMapScriptUrl(model);
        return "image-post-detail";
    }

    /**
     * 게시글 작성 폼. Step 13: 위치(위도·경도) 또는 Pin 선택.
     */
    @GetMapping("/posts/create")
    public String postCreateForm(Model model) {
        User user = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        model.addAttribute("pins", pinService.getListByOwner(user, PageRequest.of(0, 100)));
        addMapScriptUrl(model);
        return "post-create";
    }

    /**
     * 게시글 작성 폼 제출. Step 13.
     */
    @PostMapping("/posts")
    public String postCreateSubmit(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Long pinId,
            RedirectAttributes redirectAttributes) {
        User author = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        var request = new PostCreateRequest(title, content, latitude, longitude, pinId);
        var created = postService.create(request, author);
        log.info("웹 게시글 작성: postId={}, authorId={}", created.id(), author.getId());
        redirectAttributes.addFlashAttribute("message", "게시글이 작성되었습니다.");
        return "redirect:/posts/" + created.id();
    }

    /**
     * 이미지 게시글 작성 폼. Step 13.
     */
    @GetMapping("/image-posts/create")
    public String imagePostCreateForm(Model model) {
        User user = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        model.addAttribute("pins", pinService.getListByOwner(user, PageRequest.of(0, 100)));
        addMapScriptUrl(model);
        return "image-post-create";
    }

    /**
     * 이미지 게시글 작성 폼 제출. Step 13.
     */
    @PostMapping("/image-posts")
    public String imagePostCreateSubmit(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam MultipartFile image,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Long pinId,
            RedirectAttributes redirectAttributes) {
        User author = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (image == null || image.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일이 필요합니다.");
            return "redirect:/image-posts/create";
        }
        var created = imagePostService.create(title, content, image, latitude, longitude, pinId, author);
        log.info("웹 이미지 게시글 작성: imagePostId={}, authorId={}", created.id(), author.getId());
        redirectAttributes.addFlashAttribute("message", "이미지 게시글이 작성되었습니다.");
        return "redirect:/image-posts/" + created.id();
    }

    private void addMapScriptUrl(Model model) {
        String key = mapProperties.kakaoJsAppKey() != null ? mapProperties.kakaoJsAppKey() : "";
        model.addAttribute("kakaoMapScriptUrl",
                !key.isEmpty() ? "https://dapi.kakao.com/v2/maps/sdk.js?appkey=" + key + "&autoload=false" : "");
    }
}
