package com.example.sns.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sns.domain.User;
import com.example.sns.domain.UserRole;
import com.example.sns.dto.request.MemberJoinRequest;
import com.example.sns.dto.response.MemberResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 서비스.
 *
 * RULE 2.3: 트랜잭션 경계 Service 계층.
 * RULE 1.5.6: BCrypt 비밀번호 해싱.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입.
     *
     * @param request 가입 요청
     * @return 생성된 회원 응답
     * @throws BusinessException 중복 이메일 시 DUPLICATE_EMAIL
     */
    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("회원가입 실패: 중복 이메일, email={}", request.email());
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordHash)
                .nickname(request.nickname())
                .role(UserRole.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("회원가입 성공: userId={}, email={}", saved.getId(), saved.getEmail());
        return MemberResponse.from(saved);
    }

    /**
     * 회원 ID로 조회.
     *
     * @param id 회원 ID
     * @return 회원 응답
     * @throws BusinessException 존재하지 않으면 NOT_FOUND
     */
    @Transactional(readOnly = true)
    public MemberResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        return MemberResponse.from(user);
    }
}
