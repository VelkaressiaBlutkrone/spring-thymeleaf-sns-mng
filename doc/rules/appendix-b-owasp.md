# 부록 B: OWASP Top 10 2025 매핑표

> **참고**: OWASP Top 10의 **공식 최신 버전**은 2021일 수 있다. "2025"는 예상·드래프트 기반일 수 있으므로, 공식 발표 여부를 확인한 뒤 참조한다. 원본: [RULE.md](../RULE.md) 부록 B.

| 순위 | 코드     | 영문 명칭                                | 2021 대비           | 주요 위험 및 대응                                                   |
| ---- | -------- | ---------------------------------------- | ------------------- | ------------------------------------------------------------------- |
| 1    | A01:2025 | Broken Access Control                    | #1 유지 (SSRF 통합) | IDOR, 강제 브라우징, CORS 오설정, SSRF → [1.2](01-security.md)      |
| 2    | A02:2025 | Security Misconfiguration                | #5 → #2 급상승      | 기본값 노출, 포트·버킷 공개, 에러 정보 유출 → [1.6](01-security.md) |
| 3    | A03:2025 | Software Supply Chain Failures           | 신규                | 의존성·빌드·배포 체인 CVE 모니터링 → [1.7](01-security.md)          |
| 4    | A04:2025 | Cryptographic Failures                   | #2 → #4             | 약한 알고리즘, 평문 전송, 키 관리 미흡 → [1.5.2](01-security.md)    |
| 5    | A05:2025 | Injection                                | #3 → #5             | SQL/NoSQL/OS/LDAP 인젝션 → [1.3](01-security.md)                    |
| 6    | A06:2025 | Insecure Design                          | #4 → #6             | 위협 모델링 부족, 안전 설계 패턴 → [2.1](02-function.md)            |
| 7    | A07:2025 | Authentication Failures                  | 순위 유지           | 인증 우회, 스터핑, 세션 고정, 브루트포스 → [1.5.6](01-security.md)  |
| 8    | A08:2025 | Software and Data Integrity Failures     | 순위 유지           | CI/CD, deserialization 무결성 → [1.5.3](01-security.md)             |
| 9    | A09:2025 | Security Logging and Monitoring Failures | 순위 유지           | 로그 누락·조작, 모니터링 미흡 → [1.4](01-security.md)               |
| 10   | A10:2025 | Mishandling of Exceptional Conditions    | 신규                | 스택 트레이스 노출, DoS, 상태 불일치 → [2.2](02-function.md)        |
