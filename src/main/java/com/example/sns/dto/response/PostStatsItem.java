package com.example.sns.dto.response;

/**
 * 글 통계 단위별 항목. Step 17.
 *
 * @param periodLabel 기간 라벨 (예: "2026-02-01", "2026-02-W1")
 * @param postCount   일반 게시글 수
 * @param imagePostCount 이미지 게시글 수
 */
public record PostStatsItem(String periodLabel, long postCount, long imagePostCount) {
}
