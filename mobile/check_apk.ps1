$f = Get-Item 'C:\Users\serhi\Documents\Development\desktop-android-sharing\habit-pet.apk'
Write-Host "Exists: $($f.Exists)"
Write-Host "Size: $([math]::Round($f.Length/1MB,1)) MB"
Write-Host "Updated: $($f.LastWriteTime)"
