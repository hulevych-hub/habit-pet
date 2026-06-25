# Kill all java processes to release file locks
Write-Host "Killing all Java processes..."
Get-Process -Name 'java' -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "  Killing: $($_.ProcessName) (ID: $($_.Id))"
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 2

# Clean test results directories
Write-Host "Cleaning test results directories..."
$dirs = @(
    "C:\Users\serhi\Documents\Development\habit-pet\mobile\app\build\test-results",
    "C:\Users\serhi\Documents\Development\habit-pet\mobile\app\build-test-alt\test-results"
)
foreach ($dir in $dirs) {
    if (Test-Path $dir) {
        try {
            Remove-Item -Recurse -Force $dir -ErrorAction Stop
            Write-Host "  Cleaned: $dir"
        } catch {
            Write-Host "  Failed to clean: $dir - $($_.Exception.Message)"
        }
    }
}

Write-Host "`nDone. You can now run tests."
Starting a Gradle Daemon, 1 busy and 3 stopped Daemons could not be reused, use --status for details
