# 9. Observability & Distributed Tracing [MUST / SHOULD]

> 장애 대응 및 성능 분석을 위한 관찰 가능성(Observability). traceId 없는 로그 → 상관관계 불가 → 장애 대응 지연. 프로젝트 규모에 따라 적용 강도를 조정할 수 있다. 원본: [RULE.md](../RULE.md) 9장.

### 9.1 Observability 기본 원칙

| #   | Rule                                                                                                  | 레벨   | 비고                         |
| --- | ----------------------------------------------------------------------------------------------------- | ------ | ---------------------------- |
| 1   | **구조화 로깅** + **traceId(및 spanId) MDC 삽입**으로 로그-트레이스 상관관계 확보                     | MUST   | 초기 MVP·소규모도 필수       |
| 2   | **로그와 트레이스 상관관계**: traceId, spanId를 MDC에 자동 삽입                                       | MUST   |                              |
| 3   | **OpenTelemetry**를 통한 Traces, Metrics, Logs 통합 수집 (3 signals unified)                          | SHOULD | 풀셋은 규모·단계에 따라 적용 |
| 4   | **샘플링 전략 명시**: 개발/스테이징 = 100%, 운영 = head-based 10~20% (critical path는 100%)           | SHOULD |                              |
| 5   | **Vendor-neutral OTLP 프로토콜** 사용 (Jaeger, Zipkin, Tempo, Grafana Cloud, New Relic 등으로 export) | SHOULD |                              |

### 9.2 Spring Boot 구현 가이드 (2025~2026 베스트)

- **의존성**: `spring-boot-starter-opentelemetry` (Spring Boot 4.0+ 네이티브 지원)
- **자동 인스트루먼테이션**: Spring Web, JDBC, Kafka, Redis 등 자동 적용
- **수동 인스트루먼테이션** (비즈니스 로직 관찰 필요 시): Micrometer Observation → OpenTelemetry Span 자동 브릿지 (starter가 처리)
- **Collector 설정**: OpenTelemetry Collector 배포 → OTLP/gRPC 수신 → backend export
- **로그 상관관계 설정**: logback 또는 log4j2에 opentelemetry appenders 적용

### 9.3 Alerting & SLO 연계 [MUST]

| 항목              | Rule                                                            |
| ----------------- | --------------------------------------------------------------- |
| 알림              | ERROR 이상 + 비즈니스 critical span → 즉시 PagerDuty/Slack 알림 |
| SLO 기반 alerting | 예: 99% 요청 latency < 500ms, error rate < 0.1%                 |
| 도구              | Prometheus + Grafana 또는 Grafana Cloud 활용 권장               |

### 9.4 금지 사항

- ❌ **traceId 없는 로그** (상관관계 불가 → 장애 대응 지연)
- ❌ **100% 샘플링 운영 환경 적용** (비용 폭증 + 성능 저하)
