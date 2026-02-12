# 개발 환경변수 설정 후 Spring Boot 실행
# 사용: 복사하여 run-dev.ps1 로 저장한 뒤 실제 키로 교체
$env:MAP_KAKAO_JS_APP_KEY = "발급받은_JavaScript_키"
$env:MAP_KAKAO_MOBILITY_API_KEY = "발급받은_Mobility_API_키"
.\gradlew bootRun
