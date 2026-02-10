package com.example.sns.dto.response;

/**
 * 관리자 로그인 통계 응답. Step 17.
 *
 * @param loginCount   기간 내 로그인 횟수
 * @param activeUsers  기간 내 로그인한 고유 사용자 수
 */
public record LoginStatsResponse(long loginCount, long activeUsers) {
}
