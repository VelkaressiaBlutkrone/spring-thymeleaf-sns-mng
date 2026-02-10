package com.example.sns.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sns.dto.response.LoginStatsResponse;
import com.example.sns.dto.response.PostStatsItem;
import com.example.sns.dto.response.PostStatsResponse;
import com.example.sns.dto.response.SignupStatsResponse;
import com.example.sns.repository.ImagePostRepository;
import com.example.sns.repository.LoginLogRepository;
import com.example.sns.repository.PostRepository;
import com.example.sns.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 통계 서비스. Step 17.
 *
 * RULE 2.3: readOnly 트랜잭션, 쿼리 부하 고려.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PostRepository postRepository;
    private final ImagePostRepository imagePostRepository;

    /**
     * 기간별 가입자 수.
     */
    @Transactional(readOnly = true)
    public SignupStatsResponse getSignupStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        long count = userRepository.countByCreatedAtBetween(start, end);
        log.debug("가입 통계: start={}, end={}, count={}", startDate, endDate, count);
        return new SignupStatsResponse(count);
    }

    /**
     * 기간별 로그인 수·활성 사용자 수.
     */
    @Transactional(readOnly = true)
    public LoginStatsResponse getLoginStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        long loginCount = loginLogRepository.countByLoggedAtBetween(start, end);
        long activeUsers = loginLogRepository.countDistinctUserIdByLoggedAtBetween(start, end);
        log.debug("로그인 통계: start={}, end={}, loginCount={}, activeUsers={}", startDate, endDate, loginCount, activeUsers);
        return new LoginStatsResponse(loginCount, activeUsers);
    }

    /**
     * 기간별 글 통계. unit=day만 지원 (week/month/quarter/year는 day로 집계 후 라벨만 변경 가능).
     */
    @Transactional(readOnly = true)
    public PostStatsResponse getPostStats(LocalDate startDate, LocalDate endDate, String unit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Object[]> postRows = postRepository.countByCreatedAtBetweenGroupByDay(start, end);
        List<Object[]> imagePostRows = imagePostRepository.countByCreatedAtBetweenGroupByDay(start, end);

        Map<String, Long> postByDay = toMap(postRows);
        Map<String, Long> imagePostByDay = toMap(imagePostRows);

        List<PostStatsItem> items = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            String label = d.format(DATE_LABEL);
            long postCount = postByDay.getOrDefault(label, 0L);
            long imagePostCount = imagePostByDay.getOrDefault(label, 0L);
            items.add(new PostStatsItem(label, postCount, imagePostCount));
        }
        log.debug("글 통계: start={}, end={}, unit={}, items={}", startDate, endDate, unit, items.size());
        return new PostStatsResponse(unit, items);
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Object dateObj = row[0];
            Object countObj = row[1];
            String label = formatDateLabel(dateObj);
            long count = countObj instanceof Number n ? n.longValue() : 0L;
            if (label != null) {
                map.put(label, count);
            }
        }
        return map;
    }

    private String formatDateLabel(Object dateObj) {
        if (dateObj == null) return null;
        if (dateObj instanceof LocalDate d) return d.format(DATE_LABEL);
        if (dateObj instanceof java.sql.Date d) return d.toLocalDate().format(DATE_LABEL);
        return dateObj.toString();
    }
}
