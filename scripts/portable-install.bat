@echo off
echo SmartWatts Portable Installation
echo ================================

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not installed. Please install Docker Desktop first.
    echo Visit: https://docs.docker.com/desktop/windows/install/
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Compose is not installed. Please install Docker Compose first.
    pause
    exit /b 1
)

echo Creating data directories...
mkdir data\postgres 2>nul
mkdir data\redis 2>nul
mkdir data\edge-gateway 2>nul
mkdir data\backups 2>nul

echo Copying environment files...
if not exist .env (
    copy config\environment.env .env
    echo Environment configuration created
)

echo Starting SmartWatts services...
docker-compose -f docker-compose.yml up -d

echo Waiting for services to start...
timeout /t 30 /nobreak >nul

echo Checking service health...
call scripts\health-check.bat

echo.
echo Installation Complete!
echo =====================
echo SmartWatts is now running on:
echo   • Dashboard: http://localhost:3000
echo   • API Gateway: http://localhost:8080
echo   • Edge Gateway: http://localhost:8088
echo.
echo To manage services:
echo   • Start: scripts\start-smartwatts.bat
echo   • Stop: scripts\stop-smartwatts.bat
echo   • Health Check: scripts\health-check.bat
echo   • Backup: scripts\backup-data.bat
echo.
echo For hardware integration, see:
echo   • docs\Hardware_Integration_Guide.md
echo   • docs\Raspberry_Pi_5_Setup_Guide.md
pause


