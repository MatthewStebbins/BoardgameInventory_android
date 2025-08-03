# How to Fix Google APIs vs Google Play Services Issue

## The Problem Explained

Your current emulator has **"Google APIs"** which includes:
- ✅ Basic Google services (Maps, etc.)
- ❌ **No Google Play Store**
- ❌ **No Google Play Services** (required for AdMob)

You need **"Google Play"** which includes:
- ✅ Full Google Play Store
- ✅ **Google Play Services** (enables AdMob)
- ✅ All Google APIs

## Solution: Create New AVD with Google Play

### Method 1: Android Studio AVD Manager (Recommended)

1. **Open Android Studio**
2. **Go to Tools → AVD Manager**
3. **Click "Create Virtual Device"**
4. **Select Device**: Choose **Pixel 3a** (or any device you prefer)
5. **Click Next**

#### CRITICAL STEP - System Image Selection:
6. **Look for API 34** but **pay attention to the Target column**:
   - ❌ **Skip**: "API 34 Google APIs" (this is what you have now)
   - ✅ **Choose**: "API 34 **Google Play**" (this has Play Store icon)

7. **If you don't see Google Play option**:
   - Click the **Download** link next to "API 34 Google Play"
   - Wait for download to complete
   - Select it once downloaded

8. **Click Next**
9. **Name it**: "Pixel_3a_API_34_GooglePlay"
10. **Click Finish**

### Method 2: Command Line (Alternative)

```powershell
# List available system images
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --list | findstr "google_apis_playstore"

# Download Google Play system image
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" "system-images;android-34;google_apis_playstore;x86_64"

# Create new AVD
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\avdmanager.bat" create avd -n "Pixel_3a_GooglePlay" -k "system-images;android-34;google_apis_playstore;x86_64" -d "pixel_3a"
```

## How to Verify You Have the Right Image

### Visual Check in AVD Manager:
- ✅ **Target**: Should say "Google Play" (not "Google APIs")
- ✅ **Play Store**: Should show a Play Store icon ✓

### After Starting Emulator:
1. **Look for Google Play Store app** on home screen
2. **Open Play Store** - it should work and ask you to sign in
3. **If Play Store works** → You have Google Play Services → AdMob will work

## Start Your New Google Play Emulator

Once created:
1. **Close current emulator**
2. **Start the new Google Play emulator**
3. **Wait for full boot** (may take 5-10 minutes first time)
4. **Test Google Play Store** - sign in and search for an app
5. **Install our app**: `./gradlew installDebug`

## Expected Results with Google Play Services

After switching to Google Play emulator and running our app:
- **Red strip**: "AD CONTAINER TEST" (same as before)
- **Yellow strip → Green**: Ads load successfully! 🎉
- **Yellow strip → Orange**: Ad error with specific details (but at least it tries)
- **No more infinite yellow**: AdMob will actually respond

## Why This Fixes AdMob

- **Google APIs**: Basic services, no Play Store, no AdMob support
- **Google Play**: Full Play Services, AdMob SDK can communicate with Google servers

## Quick Command to Start New Emulator

Once you create the Google Play AVD:

```powershell
# List all AVDs to find your new one
& "$env:ANDROID_HOME\emulator\emulator.exe" -list-avds

# Start the Google Play emulator
& "$env:ANDROID_HOME\emulator\emulator.exe" -avd "Pixel_3a_API_34_GooglePlay"
```

This will **definitely fix** your infinite yellow strip issue because AdMob will finally have the Google Play Services it needs to function properly!
