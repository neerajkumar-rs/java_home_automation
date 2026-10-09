@echo off
echo ============================================
echo SECURE SCHEMA DEPLOYMENT SCRIPT
echo ============================================
echo.
echo This script will apply the secure database schema
echo with strict data isolation features.
echo.
echo WARNING: Backup your database first!
echo.

echo Step 1: Creating backup...
copy "home_automation.db" "home_automation_backup_%DATE:/=-%_%TIME::=-%.db"
echo Backup created as home_automation_backup_%DATE:/=-%_%TIME::=-%.db
echo.

echo Step 2: Stopping Spring Boot application...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul
echo.

echo Step 3: Applying secure schema...
sqlite3 home_automation.db ".read src\main\resources\schema_final.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Schema application failed!
    echo Restoring from backup...
    copy "home_automation_backup_%DATE:/=-%_%TIME::=-%.db" "home_automation.db"
    echo Backup restored. Please check the schema file.
    pause
    exit /b 1
)
echo Schema applied successfully!
echo.

echo Step 4: Verifying schema...
sqlite3 home_automation.db "SELECT 'Users:', COUNT(*) FROM users UNION ALL SELECT 'Devices:', COUNT(*) FROM devices;"
echo.

echo Step 5: Starting Spring Boot application...
start /B mvnw spring-boot:run
echo Application starting...
timeout /t 5 /nobreak >nul
echo.

echo Step 6: Testing authentication...
curl -X POST http://localhost:8081/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^
  -w "\nResponse Code: %%{http_code}\n" 2>nul
echo.

echo ============================================
echo SCHEMA DEPLOYMENT COMPLETE!
echo ============================================
echo.
echo Default credentials:
echo   Admin: admin@homeautomation.com / admin123
echo   User:  user@homeautomation.com / user123
echo.
echo Secure endpoints available at:
echo   GET  /api/v1/secure/devices
echo   POST /api/auth/login
echo.
echo IMPORTANT: Update your frontend to use:
echo   - /api/v1/secure/devices (with JWT token)
echo.
pause