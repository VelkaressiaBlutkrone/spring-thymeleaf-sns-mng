package com.example.sns.dto.response;

/**
 * 관리자 가입 통계 응답. Step 17.
 *
 * @param totalCount 기간 내 가입자 수
 */
public record SignupStatsResponse(long totalCount) {
}
