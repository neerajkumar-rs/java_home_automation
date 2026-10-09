@echo off
echo ============================================
echo STARTING BOTH PORTALS USING JAR FILES
echo ============================================
echo.
echo Step 1: Killing existing Java processes...
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
timeout /t 2 >nul

echo Step 2: Starting Admin Portal on port 8081...
start "Admin Portal" cmd /c "java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081"
echo Admin Portal starting... Waiting 5 seconds.
timeout /t 5 >nul

echo Step 3: Starting User Portal on port 8080...
start "User Portal" cmd /c "java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8080"
echo User Portal starting... Waiting 5 seconds.
timeout /t 5 >nul

echo Step 4: Opening portals in browser...
start "" "http://localhost:8081/admin-login.html"
start "" "http://localhost:8080/user-login.html"

echo ============================================
echo PORTALS STARTED!
echo ============================================
echo Admin Portal: http://localhost:8081/admin-login.html
echo User Portal:  http://localhost:8080/user-login.html
echo.
echo Default Credentials:
echo   Admin: admin / admin123
echo   User:  user  / user123
echo ============================================
echo NOTE: Keep both terminal windows open!
echo The applications are now running.
echo ============================================
pause