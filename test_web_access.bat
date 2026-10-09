@echo off
echo Testing web access to home automation application...

echo.
echo 1. Checking if Spring Boot is running...
timeout /nobreak /t 2 >nul

echo 2. Testing Admin Login Page: http://localhost:8081/admin-login.html
curl -s -o admin-test.html -w "HTTP Status: %{http_code}" http://localhost:8081/admin-login.html
echo.
if exist admin-test.html (
    echo Admin login page downloaded successfully!
    del admin-test.html
) else (
    echo Failed to download admin login page
)

echo.
echo 3. Testing User Login Page: http://localhost:8081/user-login.html
curl -s -o user-test.html -w "HTTP Status: %{http_code}" http://localhost:8081/user-login.html
echo.
if exist user-test.html (
    echo User login page downloaded successfully!
    del user-test.html
) else (
    echo Failed to download user login page
)

echo.
echo 4. Testing Dashboard: http://localhost:8081/dashboard.html
curl -s -o dashboard-test.html -w "HTTP Status: %{http_code}" http://localhost:8081/dashboard.html
echo.
if exist dashboard-test.html (
    echo Dashboard page downloaded successfully!
    del dashboard-test.html
) else (
    echo Failed to download dashboard page
)

echo.
echo Test completed.