@echo off
REM 검색 v1/v2 k6 부하테스트 - 각 약 70초. 요약 화면을 캡처하세요.
cd /d %~dp0
echo ============================================
echo ===  v1 (캐시 미적용 - DB 조회)  ===
echo ============================================
k6 run -e TARGET=v1 search_load.js
echo.
echo ============================================
echo ===  v2 (Redis 캐시 적용)  ===
echo ============================================
k6 run -e TARGET=v2 search_load.js
echo.
echo [완료] 위 두 요약(TOTAL RESULTS) 박스를 각각 캡처하세요.
pause
