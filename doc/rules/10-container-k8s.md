# 10. Container & Kubernetes Security [MUST / SHOULD 혼합]

> **적용 조건**: **컨테이너(Docker) 또는 Kubernetes 환경을 사용하는 경우**에만 본 장을 적용한다. 해당하지 않는 프로젝트는 이 섹션을 넘긴다.
> 원본: [RULE.md](../RULE.md) 10장. OWASP Docker Security Cheat Sheet, Kubernetes Pod Security Standards (Restricted 수준), CIS Kubernetes Benchmarks 등 준수.

### 10.1 Container Image Security [MUST]

- **최소 베이스 이미지 사용**: distroless, alpine 기반 또는 Chainguard/Wolfi 같은 hardened 이미지 우선. ❌ full OS 이미지 사용 금지.
- **이미지 태그 고정 (Pinned versions)**: `latest` 또는 floating tag 사용 금지. CI/CD에서 digest 검증 필수.
- **자동 이미지 스캐닝** [MUST]: CI 빌드 단계에서 **Trivy** 또는 **Grype** 필수 실행. Critical/High 취약점 발견 시 빌드 실패. SBOM 생성 및 CycloneDX/SPDX 형식 아카이빙 (1.7 연계).
- **이미지 서명 및 검증** [MUST 권장]: Cosign (Sigstore) 또는 Notation으로 서명. 배포 시 admission controller (Kyverno/OPA Gatekeeper)에서 서명 검증 강제.

### 10.2 Runtime Security & Pod Hardening [MUST]

- **Pod Security Standards (PSS)**: 모든 네임스페이스에 **restricted** 레벨 기본 적용. runAsNonRoot, allowPrivilegeEscalation: false, capabilities: drop ALL, seccompProfile 등.
- **SecurityContext 기본 설정** [MUST]: runAsNonRoot, runAsUser/Group/fsGroup, allowPrivilegeEscalation: false, capabilities.drop: ["ALL"], seccompProfile: RuntimeDefault.
- **금지**: privileged: true, root 실행, hostPath 마운트(필수 시 readOnly+제한 경로), hostNetwork/hostPID/hostIPC.

### 10.3 Network & Access Control [MUST]

- **NetworkPolicy 기본 적용**: 기본 deny-all + whitelist. ingress/egress 명시적 정의.
- **RBAC 최소 권한**: ClusterRole 대신 Role 사용. ServiceAccount 토큰 자동 마운트 비활성화.

### 10.4 Secrets & Config Management [MUST]

- Kubernetes Secrets 대신 **Vault** 또는 **AWS Secrets Store CSI Driver** 사용 권장. Secrets 마운트 시 read-only + tmpfs. Long-lived SA 토큰 금지 → IRSA 또는 Workload Identity.

### 10.5 Monitoring & Runtime Protection [SHOULD]

- Falco 또는 Sysdig 등 런타임 보안 도구. OpenTelemetry로 컨테이너 메트릭/트레이스 수집 (9장 연계). Image/컨테이너 drift 탐지.

### 10.6 문서화 & 체크리스트

- Helm 차트/Deployment에 SecurityContext 및 PSS 레이블 적용 여부 명시. 정기 CIS Kubernetes Benchmark 스캔(kube-bench) 및 리포트 아카이빙. 취약 이미지/파드 발견 시 remediation 계획 문서화.
