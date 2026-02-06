-- ============================================================
-- 테스트/개발용 시드 데이터 (H2 + local 프로파일 전용)
-- 구현 기능: 회원, 게시글, 이미지 게시글, Pin
-- 비밀번호: password (BCrypt)
-- ============================================================

-- 1) 회원 (USER 2명, ADMIN 1명)
INSERT INTO users (email, password_hash, nickname, role, created_at, updated_at) VALUES
('user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '테스트유저1', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '테스트유저2', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '관리자', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2) Pin (user1: 2개, user2: 1개)
INSERT INTO pins (user_id, description, latitude, longitude, created_at, updated_at) VALUES
(1, '서울시청 근처 핀', 37.5665, 126.9780, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '강남역 핀', 37.4979, 127.0276, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '홍대입구 핀', 37.5563, 126.9240, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3) 게시글 (Post) - pin 연결 포함
INSERT INTO posts (user_id, title, content, latitude, longitude, pin_id, notice, created_at, updated_at) VALUES
(1, '서울시청 방문 후기', '서울시청 주변을 구경했습니다. 날씨가 좋았어요.', 37.5665, 126.9780, 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '강남역 맛집 추천', '강남역 근처 맛집 정보를 공유합니다.', 37.4979, 127.0276, 2, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '홍대 거리 예술', '홍대에서 본 거리 예술 작품들.', 37.5563, 126.9240, 3, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '공지사항: 서비스 오픈', '지도 기반 SNS 서비스가 오픈했습니다. 많은 이용 바랍니다.', NULL, NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4) 이미지 게시글 (ImagePost) - image_url은 테스트용 더미 경로
INSERT INTO image_posts (user_id, title, content, image_url, latitude, longitude, pin_id, notice, created_at, updated_at) VALUES
(1, '서울시청 사진', '서울시청 앞에서 찍은 사진입니다.', 'image-posts/dummy-test-1.jpg', 37.5665, 126.9780, 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '홍대 야경', '홍대 입구역 야경 사진.', 'image-posts/dummy-test-2.jpg', 37.5563, 126.9240, 3, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
