$env:ANDROID_HOME="F:\Android Stuff & Cache\Sdk"
$env:ANDROID_SDK_ROOT="F:\Android Stuff & Cache\Sdk"
Write-Host "Starting Pixel 6 Emulator..." -ForegroundColor Green
& "F:\Android Stuff & Cache\Sdk\emulator\emulator.exe" -avd Pixel_6
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start emulator. Press any key to exit..." -ForegroundColor Red
    [void]$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}
