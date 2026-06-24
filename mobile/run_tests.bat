@echo off
echo Killing all Java processes...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo Cleaning test results...
rmdir /s /q "app\build\test-results" 2>nul
rmdir /s /q "app\build-test-alt\test-results" 2>nul

echo Running tests...
call gradlew :app:testDebugUnitTest --no-daemon

echo.
echo Exit code: %ERRORLEVEL%
