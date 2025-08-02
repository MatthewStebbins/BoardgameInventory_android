# Copy custom launcher icons to Android project
$sourceBase = "C:\Users\Wouph\Downloads\ic_launcher-688d7c62da668\ic_launcher-688d7c62da668\android"
$targetBase = "c:\Users\Wouph\Documents\BoardgameInventory_Android\app\src\main\res"

Write-Host "Copying custom launcher icons..." -ForegroundColor Green

$copiedCount = 0
$densities = @("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")

foreach ($density in $densities) {
    $sourceFolder = "$sourceBase\mipmap-$density"
    $targetFolder = "$targetBase\mipmap-$density"
    
    if (Test-Path $sourceFolder) {
        # Copy all PNG files from source to target
        $pngFiles = Get-ChildItem "$sourceFolder\*.png"
        foreach ($file in $pngFiles) {
            $targetFile = "$targetFolder\$($file.Name)"
            Copy-Item $file.FullName $targetFile -Force
            Write-Host "Copied: $density/$($file.Name)" -ForegroundColor Yellow
            $copiedCount++
        }
    }
}

# Copy the adaptive icon XML files
$adaptiveSource = "$sourceBase\mipmap-anydpi-v26"
$adaptiveTarget = "$targetBase\mipmap-anydpi-v26"

if (Test-Path $adaptiveSource) {
    if (-not (Test-Path $adaptiveTarget)) {
        New-Item -ItemType Directory -Path $adaptiveTarget -Force | Out-Null
    }
    
    $xmlFiles = Get-ChildItem "$adaptiveSource\*.xml"
    foreach ($file in $xmlFiles) {
        $targetFile = "$adaptiveTarget\$($file.Name)"
        Copy-Item $file.FullName $targetFile -Force
        Write-Host "Copied: anydpi-v26/$($file.Name)" -ForegroundColor Yellow
        $copiedCount++
    }
}

# Copy the background color XML
$valuesSource = "$sourceBase\values\ic_launcher_background.xml"
$valuesTarget = "$targetBase\values\ic_launcher_background.xml"

if (Test-Path $valuesSource) {
    if (-not (Test-Path "$targetBase\values")) {
        New-Item -ItemType Directory -Path "$targetBase\values" -Force | Out-Null
    }
    Copy-Item $valuesSource $valuesTarget -Force
    Write-Host "Copied: values/ic_launcher_background.xml" -ForegroundColor Yellow
    $copiedCount++
}

Write-Host ""
Write-Host "Total files copied: $copiedCount" -ForegroundColor Cyan
Write-Host "Custom launcher icons have been installed!" -ForegroundColor Green
