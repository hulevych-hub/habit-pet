$file = "C:\Users\serhi\Documents\Development\habit-pet\mobile\app\build\test-results\testDebugUnitTest\binary\output.bin"
Write-Host "File exists: $(Test-Path $file)"

$javaProcs = Get-Process -Name 'java' -ErrorAction SilentlyContinue
Write-Host "Java processes: $($javaProcs.Count)"
$javaProcs | ForEach-Object { Write-Host "  Id=$($_.Id) Memory=$([math]::Round($_.WorkingSet64/1MB,1))MB" }

try {
    $fs = [System.IO.File]::Open($file, 'Open', 'ReadWrite', 'None')
    $fs.Close()
    Write-Host "File is NOT locked"
} catch {
    Write-Host "File IS locked: $($_.Exception.Message)"
}
