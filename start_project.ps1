# Enhanced VS Code Restart Script for BoardGame Inventory Android Project
# This script ensures proper Java environment and VS Code configuration

Write-Host "=== BoardGame Inventory Android Project Setup ===" -ForegroundColor Green
Write-Host ""

# Set Java environment
$env:JAVA_HOME = "C:\Users\Wouph\Documents\BoardgameInventory_Android\jdk-17.0.12"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Java Environment:" -ForegroundColor Yellow
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host ""

# Test Java version
Write-Host "Java Version Check:" -ForegroundColor Yellow
& "$env:JAVA_HOME\bin\java.exe" -version
Write-Host ""

# Test Gradle with correct Java
Write-Host "Gradle Version Check:" -ForegroundColor Yellow
.\gradlew --version
Write-Host ""

# Clean up any lock files and processes
Write-Host "Cleaning up lock files and processes..." -ForegroundColor Yellow
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "gradle*" -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".gradle\daemon" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue

# Wait for cleanup
Start-Sleep -Seconds 2

Write-Host "Testing Gradle clean..." -ForegroundColor Yellow
$cleanResult = .\gradlew clean --info 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Gradle clean successful" -ForegroundColor Green
} else {
    Write-Host "⚠️ Gradle clean had issues, but continuing..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Stopping VS Code processes..." -ForegroundColor Yellow

# Close any existing VS Code instances
Get-Process | Where-Object {$_.ProcessName -eq "Code"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Wait for VS Code to close
Write-Host "Waiting for VS Code to close..."
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Starting VS Code with proper environment..." -ForegroundColor Green

# Start VS Code with the workspace and proper environment
$env:VSCODE_JAVA_HOME = $env:JAVA_HOME
& code "C:\Users\Wouph\Documents\BoardgameInventory_Android"

Write-Host ""
Write-Host "=== Setup Complete ===" -ForegroundColor Green
Write-Host "VS Code should now load the project properly with Java 17" -ForegroundColor Cyan
Write-Host "The Java and Kotlin language servers should start correctly" -ForegroundColor Cyan
Write-Host ""
