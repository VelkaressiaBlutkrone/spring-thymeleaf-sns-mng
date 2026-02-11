# Flutter 연동 API 구조 예시 (Step 20)

> REST API 호출 구조, **인증(JWT Bearer, Refresh Token Secure Storage)**, 지도 SDK 연동 예시.  
> RULE 6.1.6: 모바일에서는 **flutter_secure_storage** 사용(SharedPreferences 금지).

## 1. 개요

| 항목 | 내용 |
|------|------|
| Base URL | 배포 서버 기준 예: `https://api.example.com` 또는 상대 경로 `/api` (Web) |
| 인증 | Access Token: `Authorization: Bearer {token}` |
| Refresh Token | 서버에서 Set-Cookie 또는 Body로 전달 시, **Secure Storage에만 저장** |
| API 문서 | Swagger: `{baseUrl}/swagger-ui.html` |

---

## 2. REST API 호출 구조

### 2.1 HTTP 클라이언트 (dio 예시)

```dart
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiClient {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'https://api.example.com',  // 또는 환경별로 분리
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
  ));

  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  ApiClient() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final accessToken = await _storage.read(key: 'access_token');
        if (accessToken != null) {
          options.headers['Authorization'] = 'Bearer $accessToken';
        }
        return handler.next(options);
      },
      onError: (error, handler) async {
        if (error.response?.statusCode == 401) {
          final refreshed = await _refreshToken();
          if (refreshed) {
            final again = await _retry(error.requestOptions);
            return handler.resolve(again);
          }
        }
        return handler.next(error);
      },
    ));
  }

  Future<bool> _refreshToken() async {
    final refreshToken = await _storage.read(key: 'refresh_token');
    if (refreshToken == null) return false;
    try {
      final res = await _dio.post('/api/auth/refresh',
        options: Options(
          headers: {'Cookie': 'refreshToken=$refreshToken'},
          extra: {'skipAuth': true},
        ),
      );
      final newAccess = res.data['accessToken'];
      if (newAccess != null) {
        await _storage.write(key: 'access_token', value: newAccess);
        return true;
      }
    } catch (_) {}
    return false;
  }

  Future<Response<dynamic>> _retry(RequestOptions options) {
    return _dio.fetch(options);
  }

  Future<Response<dynamic>> get(String path, {Map<String, dynamic>? params}) =>
      _dio.get(path, queryParameters: params);

  Future<Response<dynamic>> post(String path, dynamic data) =>
      _dio.post(path, data: data);

  Future<Response<dynamic>> put(String path, dynamic data) =>
      _dio.put(path, data: data);

  Future<Response<dynamic>> delete(String path) => _dio.delete(path);
}
```

### 2.2 로그인 후 토큰 저장 (Secure Storage)

```dart
// POST /api/auth/login
final response = await apiClient.post('/api/auth/login', {
  'email': email,
  'password': password,
});

if (response.statusCode == 200) {
  final accessToken = response.data['accessToken'];
  // Refresh Token: 서버가 Set-Cookie로 주는 경우 웹뷰/쿠키에서 읽거나,
  // 서버가 Body에 넣어 주면 response.data['refreshToken'] 사용
  final refreshToken = response.data['refreshToken'] ?? await _readRefreshFromCookie();

  const storage = FlutterSecureStorage();
  await storage.write(key: 'access_token', value: accessToken);
  if (refreshToken != null) {
    await storage.write(key: 'refresh_token', value: refreshToken);
  }
}
```

- **RULE 6.1.6**: Refresh Token은 **flutter_secure_storage**에만 저장. SharedPreferences 사용 금지.

---

## 3. 인증 요청 흐름 요약

| 단계 | 설명 |
|------|------|
| 1 | 로그인: `POST /api/auth/login` → accessToken + refreshToken(또는 Set-Cookie) |
| 2 | accessToken, refreshToken을 **Secure Storage**에 저장 |
| 3 | API 요청 시 `Authorization: Bearer {accessToken}` 헤더 추가 |
| 4 | 401 수신 시 `POST /api/auth/refresh`(Cookie 또는 Body에 refreshToken) 호출 |
| 5 | 새 accessToken 저장 후 원래 요청 재시도 |
| 6 | refresh 실패 시 로그인 화면으로 이동 |

---

## 4. 지도 SDK 연동 예시

백엔드 반경 조회 API와 연동해 지도에 Pin·게시글을 표시하는 흐름.

### 4.1 반경 조회 API (백엔드)

| API | Method | URL | Query |
|-----|--------|-----|-------|
| 반경 내 Pin | GET | /api/pins/nearby | lat, lng, radiusKm, page, size |
| 반경 내 게시글 | GET | /api/posts/nearby | lat, lng, radiusKm, page, size |
| 반경 내 이미지 게시글 | GET | /api/image-posts/nearby | lat, lng, radiusKm, page, size |

- 인증 없이 호출 가능(비로그인 조회 허용).

### 4.2 Flutter 지도 패키지 선택

| 플랫폼 | 패키지 예시 | 비고 |
|--------|-------------|------|
| **Google Maps** | `google_maps_flutter` | Android/iOS API 키 필요 |
| **Kakao Map** | 카카오 지도 SDK Flutter 래퍼 또는 WebView | JavaScript API 키 사용 시 WebView |
| **OpenStreetMap** | `flutter_map` + `latlong2` | 오픈소스, API 키 불필요 |

### 4.3 연동 흐름 예시 (의사 코드)

```dart
// 1) 현재 위치 또는 지도 중심 좌표
double lat = 37.5665;
double lng = 126.9780;
double radiusKm = 5.0;

// 2) 반경 내 Pin 목록 조회
final pins = await apiClient.get('/api/pins/nearby', params: {
  'lat': lat,
  'lng': lng,
  'radiusKm': radiusKm,
  'page': 0,
  'size': 50,
});

// 3) 지도 위에 마커 표시
for (final pin in pins.data['content']) {
  addMarker(
    LatLng(pin['latitude'], pin['longitude']),
    title: pin['description'],
    onTap: () => navigateToPinPosts(pin['id']),
  );
}
```

### 4.4 Pin별 게시글 목록

| API | Method | URL | 설명 |
|-----|--------|-----|------|
| Pin별 게시글 | GET | /api/pins/{pinId}/posts | 해당 Pin에 연결된 게시글 |
| Pin별 이미지 게시글 | GET | /api/pins/{pinId}/image-posts | 해당 Pin에 연결된 이미지 게시글 |

- Pin 마커 탭 시 위 API로 목록 조회 후 상세 화면으로 이동.

---

## 5. 에러 처리

| HTTP | 처리 예시 |
|------|-----------|
| 401 | Refresh Token으로 갱신 시도 → 실패 시 로그인 화면 |
| 403 | 권한 부족 메시지 표시 |
| 429 | Rate Limit. 응답 헤더 `Retry-After`(초) 참고 후 재시도 안내 |
| 4xx/5xx | `ErrorResponse` (code, message) 표시 |

---

## 6. 참고 문서

| 문서 | 내용 |
|------|------|
| [API_SPEC.md](./API_SPEC.md) | REST API 상세 명세 |
| [AUTH_DESIGN.md](./AUTH_DESIGN.md) | JWT·Refresh Token 설계 |
| [MAP_API.md](./MAP_API.md) | 백엔드 지도 API·반경 조회 |
| [DEPLOYMENT.md](./DEPLOYMENT.md) | 배포·환경 변수 |

---

**최종 업데이트**: 2026-02-11 (Step 20)
