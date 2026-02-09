package com.example.sns.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sns.domain.UserRole;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.service.AuthService;
import com.example.sns.service.ImagePostService;
import com.example.sns.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 게시물 관리 웹 뷰 컨트롤러.
 *
 * Step 16: ROLE_ADMIN 전용. 게시글(일반·이미지) 목록·수정·삭제·공지.
 * RULE 1.2: ROLE_ADMIN만 접근.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPostViewController {

    private final PostService postService;
    private final ImagePostService imagePostService;
    private final AuthService authService;

    /**
     * 일반 게시글 목록.
     */
    @GetMapping("/posts")
    public String postList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        ensureAdmin();
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("posts", postService.getListForAdmin(keyword, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", "posts");
        return "admin/post-list";
    }

    /**
     * 일반 게시글 상세·수정 폼.
     */
    @GetMapping("/posts/{id}")
    public String postEditForm(@PathVariable Long id, Model model) {
        ensureAdmin();
        model.addAttribute("post", postService.getById(id));
        model.addAttribute("type", "posts");
        return "admin/post-edit";
    }

    /**
     * 일반 게시글 수정 제출.
     */
    @PostMapping("/posts/{id}")
    public String postUpdateSubmit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {
        ensureAdmin();
        try {
            postService.updateByAdmin(id, new com.example.sns.dto.request.PostUpdateRequest(title, content));
            redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/posts/" + id;
    }

    /**
     * 일반 게시글 삭제.
     */
    @PostMapping("/posts/{id}/delete")
    public String postDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ensureAdmin();
        postService.deleteByAdmin(id);
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/admin/posts";
    }

    /**
     * 일반 게시글 공지 등록/해제.
     */
    @PostMapping("/posts/{id}/notice")
    public String postToggleNotice(
            @PathVariable Long id,
            @RequestParam boolean notice,
            RedirectAttributes redirectAttributes) {
        ensureAdmin();
        postService.setNotice(id, notice);
        redirectAttributes.addFlashAttribute("message", notice ? "공지로 등록되었습니다." : "공지가 해제되었습니다.");
        return "redirect:/admin/posts/" + id;
    }

    /**
     * 이미지 게시글 목록.
     */
    @GetMapping("/image-posts")
    public String imagePostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        ensureAdmin();
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("posts", imagePostService.getListForAdmin(keyword, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", "image-posts");
        return "admin/image-post-list";
    }

    /**
     * 이미지 게시글 상세·수정 폼.
     */
    @GetMapping("/image-posts/{id}")
    public String imagePostEditForm(@PathVariable Long id, Model model) {
        ensureAdmin();
        model.addAttribute("post", imagePostService.getById(id));
        model.addAttribute("type", "image-posts");
        return "admin/image-post-edit";
    }

    /**
     * 이미지 게시글 수정 제출.
     */
    @PostMapping("/image-posts/{id}")
    public String imagePostUpdateSubmit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile image,
            RedirectAttributes redirectAttributes) {
        ensureAdmin();
        try {
            imagePostService.updateByAdmin(id, title, content, image);
            redirectAttributes.addFlashAttribute("message", "이미지 게시글이 수정되었습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/image-posts/" + id;
    }

    /**
     * 이미지 게시글 삭제.
     */
    @PostMapping("/image-posts/{id}/delete")
    public String imagePostDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ensureAdmin();
        imagePostService.deleteByAdmin(id);
        redirectAttributes.addFlashAttribute("message", "이미지 게시글이 삭제되었습니다.");
        return "redirect:/admin/image-posts";
    }

    /**
     * 이미지 게시글 공지 등록/해제.
     */
    @PostMapping("/image-posts/{id}/notice")
    public String imagePostToggleNotice(
            @PathVariable Long id,
            @RequestParam boolean notice,
            RedirectAttributes redirectAttributes) {
        ensureAdmin();
        imagePostService.setNotice(id, notice);
        redirectAttributes.addFlashAttribute("message", notice ? "공지로 등록되었습니다." : "공지가 해제되었습니다.");
        return "redirect:/admin/image-posts/" + id;
    }

    private void ensureAdmin() {
        authService.getCurrentUserEntity()
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "관리자만 접근할 수 있습니다."));
    }
}
