param(
    [int]$IntervalSeconds = 5,
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "Virtual home devices started. Press Ctrl+C to stop." -ForegroundColor Cyan
Write-Host "Polling $BaseUrl every $IntervalSeconds seconds..." -ForegroundColor DarkGray

while ($true) {
    try {
        $devices = @(Invoke-RestMethod -Uri "$BaseUrl/devices" -Method Get)
        $activeDevices = @($devices | Where-Object { $_.active })

        if ($activeDevices.Count -eq 0) {
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] No active devices to simulate." -ForegroundColor Yellow
        } else {
            $device = $activeDevices | Get-Random
            if ($device.controlType -eq "SWITCH") {
                $state = @("ON", "OFF") | Get-Random
                $value = if ($state -eq "ON") { 100 } else { 0 }
            } else {
                $value = Get-Random -Minimum 0 -Maximum 101
                $state = if ($value -gt 0) { "ON" } else { "OFF" }
            }

            $body = @{ state = $state; value = $value } | ConvertTo-Json
            $result = Invoke-RestMethod -Uri "$BaseUrl/device/$($device.id)/state" -Method Post -ContentType "application/json" -Body $body
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] VIRTUAL $($device.name) [$($device.controlType)] -> $state ($value%)" -ForegroundColor Green
        }
    } catch {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Simulator error: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Seconds $IntervalSeconds
}
