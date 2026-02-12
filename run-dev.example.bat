@echo off
REM 개발 환경변수 설정 후 Spring Boot 실행
REM 사용: 복사하여 run-dev.bat 로 저장한 뒤 실제 키로 교체
set MAP_KAKAO_JS_APP_KEY=발급받은_JavaScript_키
set MAP_KAKAO_MOBILITY_API_KEY=발급받은_Mobility_API_키
call gradlew.bat bootRun
