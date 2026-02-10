package com.example.sns.dto.response;

import java.util.List;

/**
 * 관리자 글 통계 응답. Step 17.
 *
 * @param unit  집계 단위 (day, week, month, quarter, year)
 * @param items 단위별 집계 목록
 */
public record PostStatsResponse(String unit, List<PostStatsItem> items) {
}
