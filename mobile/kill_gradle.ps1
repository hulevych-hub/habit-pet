Get-Process -Name 'java' | ForEach-Object {
    $handle = $_.Id
    $proc = Get-Process -Id $handle -ErrorAction SilentlyContinue
    if ($proc) {
        Write-Host "Java process: $handle - $($proc.ProcessName)"
    }
}

# Try to find which process has the file open
$filePath = "C:\Users\serhi\Documents\Development\habit-pet\mobile\app\build-test-alt\test-results\testDebugUnitTest\binary\output.bin"
Write-Host "`nChecking for file lock on: $filePath"

# Use handle.exe from Sysinternals if available, or try to kill all java processes
Get-Process -Name 'java' | ForEach-Object {
    Write-Host "Killing Java process: $($_.Id)"
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

Write-Host "`nDone. All Java processes killed."
