# Creating basic launcher icons
# These are minimal 1x1 blue PNG files that should satisfy the build requirement

# Base64 encoded 48x48 blue PNG
$icon48 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
$icon72 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
$icon96 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
$icon144 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

[System.Convert]::FromBase64String($icon48) | Set-Content -Path "C:\Users\Wouph\Documents\BoardgameInventory_Android\app\src\main\res\mipmap-mdpi\ic_launcher.png" -Encoding Byte
[System.Convert]::FromBase64String($icon72) | Set-Content -Path "C:\Users\Wouph\Documents\BoardgameInventory_Android\app\src\main\res\mipmap-hdpi\ic_launcher.png" -Encoding Byte
[System.Convert]::FromBase64String($icon96) | Set-Content -Path "C:\Users\Wouph\Documents\BoardgameInventory_Android\app\src\main\res\mipmap-xhdpi\ic_launcher.png" -Encoding Byte
[System.Convert]::FromBase64String($icon144) | Set-Content -Path "C:\Users\Wouph\Documents\BoardgameInventory_Android\app\src\main\res\mipmap-xxhdpi\ic_launcher.png" -Encoding Byte
