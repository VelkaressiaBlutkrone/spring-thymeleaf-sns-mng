package com.example.sns.dto.response;

/**
 * 로그인·토큰 갱신 응답 DTO.
 *
 * API 명세: accessToken, tokenType, expiresIn.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    public static LoginResponse of(String accessToken, long expiresInSeconds) {
        return new LoginResponse(accessToken, "Bearer", expiresInSeconds);
    }
}
