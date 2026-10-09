# PowerShell script to test Home Automation Authentication System
# Run this after the application has started on port 8081

$baseUrl = "http://localhost:8081/api/auth"

# Function to make HTTP requests
function Invoke-TestRequest {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
            UseDefaultCredentials = $true
        }
        
        if ($Body) {
            $params.Body = $Body | ConvertTo-Json
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        return @{
            Success = $true
            Response = $response
            StatusCode = 200
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorMessage = $_.Exception.Message
        return @{
            Success = $false
            StatusCode = $statusCode
            Error = $errorMessage
        }
    }
}

# Write colored output
function Write-TestResult {
    param(
        [string]$TestName,
        [bool]$Passed
    )
    
    if ($Passed) {
        Write-Host "[$TestName]: ✅ PASS" -ForegroundColor Green
    } else {
        Write-Host "[$TestName]: ❌ FAIL" -ForegroundColor Red
    }
}

# Test 1: Check if authentication endpoints exist
Write-Host "=== Testing Authentication Endpoints ===" -ForegroundColor Cyan

# Test login endpoint
Write-Host "`n1. Testing login functionality:" -ForegroundColor Yellow

# Test valid admin login
$loginResponse = Invoke-TestRequest -Url "$baseUrl/login" -Method POST -Body @{
    username = "admin"
    password = "admin123"
}

$adminLoginPassed = $loginResponse.Success -and $loginResponse.StatusCode -eq 200
Write-TestResult -TestName "Admin login" -Passed $adminLoginPassed
if ($adminLoginPassed) {
    $adminToken = $loginResponse.Response.token
    Write-Host "   Token received: $($adminToken.Substring(0, [Math]::Min(30, $adminToken.Length)))..." -ForegroundColor Gray
}

# Test valid user login
$userLoginResponse = Invoke-TestRequest -Url "$baseUrl/login" -Method POST -Body @{
    username = "user"
    password = "user123"
}

$userLoginPassed = $userLoginResponse.Success -and $userLoginResponse.StatusCode -eq 200
Write-TestResult -TestName "User login" -Passed $userLoginPassed

# Test invalid login
$invalidLoginResponse = Invoke-TestRequest -Url "$baseUrl/login" -Method POST -Body @{
    username = "admin"
    password = "wrongpassword"
}

$invalidLoginPassed = -not $invalidLoginResponse.Success -and $invalidLoginResponse.StatusCode -eq 401
Write-TestResult -TestName "Invalid login" -Passed $invalidLoginPassed

# Test 2: Token validation
Write-Host "`n2. Testing token validation:" -ForegroundColor Yellow

if ($adminLoginPassed -and $adminToken) {
    $validateResponse = Invoke-TestRequest -Url "$baseUrl/validate" -Method GET -Headers @{
        Authorization = "Bearer $adminToken"
    }
    
    $validatePassed = $validateResponse.Success -and $validateResponse.Response.valid -eq $true
    Write-TestResult -TestName "Token validation" -Passed $validatePassed
} else {
    Write-Host "[Token validation]: ⚠️ SKIP (no token available)" -ForegroundColor Yellow
}

# Test 3: Current user endpoint
Write-Host "`n3. Testing current user endpoint:" -ForegroundColor Yellow

if ($adminLoginPassed -and $adminToken) {
    $meResponse = Invoke-TestRequest -Url "$baseUrl/me" -Method GET -Headers @{
        Authorization = "Bearer $adminToken"
    }
    
    $mePassed = $meResponse.Success -and $meResponse.StatusCode -eq 200
    Write-TestResult -TestName "Current user data" -Passed $mePassed
    
    if ($mePassed) {
        Write-Host "   Username: $($meResponse.Response.username)" -ForegroundColor Gray
        Write-Host "   Email: $($meResponse.Response.email)" -ForegroundColor Gray
        Write-Host "   Is Admin: $($meResponse.Response.isAdmin)" -ForegroundColor Gray
    }
} else {
    Write-Host "[Current user data]: ⚠️ SKIP (no token available)" -ForegroundColor Yellow
}

# Test 4: Admin endpoints
Write-Host "`n4. Testing admin endpoints:" -ForegroundColor Yellow

if ($adminLoginPassed -and $adminToken) {
    $adminUsersResponse = Invoke-TestRequest -Url "http://localhost:8081/api/admin/users" -Method GET -Headers @{
        Authorization = "Bearer $adminToken"
    }
    
    $adminEndpointPassed = $adminUsersResponse.Success -or $adminUsersResponse.StatusCode -eq 403
    Write-TestResult -TestName "Admin users endpoint" -Passed $adminEndpointPassed
    
    if ($adminUsersResponse.Success) {
        Write-Host "   Users retrieved: $($adminUsersResponse.Response.Length)" -ForegroundColor Gray
    }
} else {
    Write-Host "[Admin users endpoint]: ⚠️ SKIP (no token available)" -ForegroundColor Yellow
}

# Test 5: Device endpoints (protected)
Write-Host "`n5. Testing device endpoints:" -ForegroundColor Yellow

# Test without token (should fail)
$devicesNoTokenResponse = Invoke-TestRequest -Url "http://localhost:8081/device/s" -Method GET
$noTokenFailPassed = -not $devicesNoTokenResponse.Success -and ($devicesNoTokenResponse.StatusCode -eq 401 -or $devicesNoTokenResponse.StatusCode -eq 403)
Write-TestResult -TestName "Devices without token" -Passed $noTokenFailPassed

# Test with token
if ($adminLoginPassed -and $adminToken) {
    $devicesWithTokenResponse = Invoke-TestRequest -Url "http://localhost:8081/device/s" -Method GET -Headers @{
        Authorization = "Bearer $adminToken"
    }
    
    $withTokenPassed = $devicesWithTokenResponse.Success -and $devicesWithTokenResponse.StatusCode -eq 200
    Write-TestResult -TestName "Devices with token" -Passed $withTokenPassed
    
    if ($withTokenPassed) {
        Write-Host "   Devices retrieved: $($devicesWithTokenResponse.Response.Length)" -ForegroundColor Gray
    }
} else {
    Write-Host "[Devices with token]: ⚠️ SKIP (no token available)" -ForegroundColor Yellow
}

# Summary
Write-Host "`n=== Test Summary ===" -ForegroundColor Cyan
Write-Host "Server URL: http://localhost:8081" -ForegroundColor White
Write-Host "`nDefault credentials:" -ForegroundColor White
Write-Host "  Admin: admin/admin123" -ForegroundColor Gray
Write-Host "  User: user/user123" -ForegroundColor Gray

Write-Host "`nPages to test manually:" -ForegroundColor White
Write-Host "  1. Login page: http://localhost:8081/login.html" -ForegroundColor Gray
Write-Host "  2. Registration: http://localhost:8081/register.html" -ForegroundColor Gray
Write-Host "  3. User dashboard: http://localhost:8081/dashboard.html" -ForegroundColor Gray
Write-Host "  4. Admin dashboard: http://localhost:8081/admin.html" -ForegroundColor Gray
Write-Host "  5. Main page (auto-redirect): http://localhost:8081/" -ForegroundColor Gray

Write-Host "`nNext steps:" -ForegroundColor White
Write-Host "  1. Open http://localhost:8081 in browser" -ForegroundColor Gray
Write-Host "  2. It should redirect to login page" -ForegroundColor Gray
Write-Host "  3. Login with admin/admin123 or user/user123" -ForegroundColor Gray
Write-Host "  4. Verify redirection to appropriate dashboard" -ForegroundColor Gray