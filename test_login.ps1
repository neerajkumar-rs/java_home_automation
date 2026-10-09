# Test login
$url = "http://localhost:8081/api/auth/login"
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Write-Host "Testing login endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "Token: $($response.token.substring(0,30))..." -ForegroundColor Cyan
    Write-Host "Username: $($response.username)" -ForegroundColor Cyan
    Write-Host "Is Admin: $($response.admin)" -ForegroundColor Cyan
    
    # Test getting devices with the token
    $token = $response.token
    Write-Host "`nTesting device endpoint..." -ForegroundColor Yellow
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
        }
        $devicesResponse = Invoke-RestMethod -Uri "http://localhost:8081/device/s" -Method Get -Headers $headers
        Write-Host "Devices retrieved successfully!" -ForegroundColor Green
        Write-Host "Number of devices: $($devicesResponse.Count)" -ForegroundColor Cyan
        if ($devicesResponse.Count -gt 0) {
            Write-Host "First device: $($devicesResponse[0].name) ($($devicesResponse[0].state))" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "Failed to get devices: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $streamReader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $streamReader.ReadToEnd()
        Write-Host "Error body: $errorBody" -ForegroundColor Red
    }
}