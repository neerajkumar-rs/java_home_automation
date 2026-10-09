@echo off
echo ============================================
echo Testing Running Home Automation Application
echo ============================================
echo.

echo Checking if application is running...
echo.

echo 1. Testing if port 8081 is in use:
netstat -an | findstr :8081 >nul
if %errorlevel% equ 0 (
    echo ✓ PORT 8081 is active - Spring Boot is running!
) else (
    echo ✗ PORT 8081 is NOT active - Application may not be running
    goto :end
)

echo.
echo 2. Application should be accessible at:
echo    - Admin Login: http://localhost:8081/admin-login.html
echo    - User Login:  http://localhost:8081/user-login.html
echo    - Dashboard:    http://localhost:8081/dashboard.html
echo    - Admin Page:  http://localhost:8081/admin.html
echo.

echo 3. To access the application:
echo    a) Open your web browser
echo    b) Navigate to http://localhost:8081/admin-login.html
echo    c) You should see a professional admin login page
echo.

echo 4. Troubleshooting:
echo    - If you see a white page, wait 5 seconds for app to fully start
echo    - If connection refused, application may not be running
echo    - Check that port 8081 is not used by another application
echo.

echo 5. To stop the application:
echo    Press Ctrl+C in the terminal where Spring Boot is running
echo.

:end
echo ============================================
echo Test Complete
echo ============================================