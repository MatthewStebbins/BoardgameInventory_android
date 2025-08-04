# Script to restart VS Code with proper Java environment and fix Kotlin Language Server
$env:JAVA_HOME = "C:\Users\Wouph\Documents\BoardgameInventory_Android\jdk-17.0.12"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Setting JAVA_HOME to: $env:JAVA_HOME"
Write-Host "Java version check:"
& "$env:JAVA_HOME\bin\java.exe" -version

Write-Host "`nDisabling conflicting Kotlin Language extension..."
try {
    code --disable-extension mathiasfrohlich.kotlin
    Write-Host "Disabled mathiasfrohlich.kotlin extension"
} catch {
    Write-Host "Could not disable extension (may not be necessary)"
}

Write-Host "`nCleaning VS Code workspace cache..."
$workspaceCacheDir = "$env:USERPROFILE\.vscode\CachedExtensions"
if (Test-Path $workspaceCacheDir) {
    Remove-Item -Path $workspaceCacheDir -Recurse -Force -ErrorAction SilentlyContinue
}

$extensionHostCacheDir = "$env:USERPROFILE\.vscode\logs"
if (Test-Path $extensionHostCacheDir) {
    Get-ChildItem -Path $extensionHostCacheDir -Filter "*exthost*" | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "`nRestarting VS Code with proper Java environment..."

# Close any existing VS Code instances for this workspace
Get-Process | Where-Object {$_.ProcessName -eq "Code"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Wait a moment for processes to close
Start-Sleep -Seconds 5

# Start VS Code with the workspace and environment variables
$env:JAVA_HOME = "C:\Users\Wouph\Documents\BoardgameInventory_Android\jdk-17.0.12"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
& code "C:\Users\Wouph\Documents\BoardgameInventory_Android"

Write-Host "`nVS Code started. Please wait 1-2 minutes for language servers to initialize."
Write-Host "Check the Output panel -> Kotlin for language server status."
