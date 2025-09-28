@echo off
echo SmartWatts Health Check
echo ========================

echo Checking Docker services...
docker-compose -f docker-compose.yml ps

echo.
echo Checking service health...

REM Check API Gateway
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ API Gateway: UP
) else (
    echo ❌ API Gateway: DOWN
)

REM Check Dashboard
curl -s http://localhost:3000 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Dashboard: UP
) else (
    echo ❌ Dashboard: DOWN
)

REM Check Edge Gateway
curl -s http://localhost:8088/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Edge Gateway: UP
) else (
    echo ❌ Edge Gateway: DOWN
)

echo.
echo Health check complete!
pause


