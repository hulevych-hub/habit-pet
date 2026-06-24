$file = "C:\Users\serhi\Documents\Development\habit-pet\mobile\app\build\test-results\testDebugUnitTest\binary\output.bin"
Write-Host "Checking: $file"

# Try using handle.exe from Sysinternals
$handleExe = "C:\Users\serhi\Documents\Development\habit-pet\mobile\handle.exe"
if (Test-Path $handleExe) {
    Write-Host "Using handle.exe..."
    & $handleExe $file
} else {
    Write-Host "handle.exe not found. Checking processes with open file handles..."
    # Try to use .NET to check for file locks
    $procs = Get-Process | Where-Object {$_.ProcessName -match 'java|gradle|test'}
    foreach ($p in $procs) {
        Write-Host "Process: $($p.ProcessName) (ID: $($p.Id)) - Memory: $($p.WorkingSet64 / 1MB) MB"
    }
}

# Try to see if we can open the file exclusively
Write-Host "`nTrying to open file exclusively..."
try {
    $fs = [System.IO.File]::Open($file, 'Open', 'ReadWrite', 'None')
    $fs.Close()
    Write-Host "File is NOT locked - can be opened exclusively"
} catch {
    Write-Host "File IS locked: $_"
}
