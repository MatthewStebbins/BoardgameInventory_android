# Security Configuration Guide

## Overview
This document explains the security improvements made to the BoardGame Inventory app.

## API Key Security

### Problem Fixed
- **Before**: API keys were hardcoded in source code (security vulnerability)
- **After**: API keys are stored in `local.properties` and loaded via BuildConfig

### Setup Instructions

1. **Get your API key**:
   - Sign up at [RapidAPI](https://rapidapi.com/)
   - Subscribe to the barcode lookup service
   - Copy your API key

2. **Configure local.properties**:
   ```properties
   # Add these lines to local.properties
   RAPIDAPI_KEY=your_actual_api_key_here
   RAPIDAPI_HOST=barcodes1.p.rapidapi.com
   ```

3. **Important Notes**:
   - `local.properties` is automatically ignored by Git (security)
   - Never commit API keys to version control
   - Use `local.properties.example` as a template

## Storage Permissions

### Problem Fixed
- **Before**: Used deprecated storage permissions
- **After**: Modern scoped storage with version-specific permissions

### Permission Strategy

#### Android 13+ (API 33+)
- `READ_MEDIA_IMAGES` - For image files
- `READ_MEDIA_VIDEO` - For video files  
- `READ_MEDIA_AUDIO` - For audio files

#### Android 11-12 (API 30-32)
- `READ_EXTERNAL_STORAGE` - Legacy permission

#### Android 10 and below (API 29-)
- `READ_EXTERNAL_STORAGE` - Read access
- `WRITE_EXTERNAL_STORAGE` - Write access

### Usage
Use the new `StoragePermissionUtils` class:

```kotlin
// Check permissions
if (StoragePermissionUtils.hasStoragePermissions(context)) {
    // Proceed with file operations
} else {
    // Request permissions
    val permissions = StoragePermissionUtils.getPermissionsToRequest(context)
    requestPermissions(permissions)
}
```

## Security Best Practices

### ✅ Implemented
- [x] API keys in BuildConfig (not source code)
- [x] Version-specific storage permissions
- [x] Proper error handling for missing API keys
- [x] Git ignore for sensitive files

### 🔄 Recommended Next Steps
- [ ] Add ProGuard/R8 obfuscation for release builds
- [ ] Implement certificate pinning for API calls
- [ ] Add biometric authentication for sensitive operations
- [ ] Use encrypted SharedPreferences for user data
- [ ] Implement proper input validation and sanitization

## File Security

### Protected Files
- `local.properties` - Contains API keys (Git ignored)
- `keystore.jks` - App signing keys (if created)
- `google-services.json` - Firebase config (if used)

### Git Ignore
The following files are automatically ignored:
```
local.properties
*.jks
*.keystore
google-services.json
```

## Build Configuration

The app now uses BuildConfig for secure configuration:

```kotlin
// In ApiClient.kt
private val RAPIDAPI_KEY = BuildConfig.RAPIDAPI_KEY
private val RAPIDAPI_HOST = BuildConfig.RAPIDAPI_HOST
```

This ensures API keys are:
- Not visible in source code
- Not committed to version control
- Properly obfuscated in release builds

## Testing

To test the security improvements:

1. **API Key**: Try building with `RAPIDAPI_KEY=your_api_key_here` (should show warning)
2. **Permissions**: Test on different Android versions to verify appropriate permissions are requested
3. **Build**: Ensure the app builds successfully with proper API key configuration

## Troubleshooting

### Build Issues
- **Error**: `Unresolved reference: BuildConfig`
  - **Solution**: Clean and rebuild the project
  - **Cause**: BuildConfig is generated during build

### Permission Issues
- **Error**: File access denied
  - **Solution**: Check if proper permissions are granted for the device's Android version
  - **Use**: `StoragePermissionUtils.hasStoragePermissions(context)`

### API Issues
- **Error**: Barcode lookup not working
  - **Solution**: Verify API key is correctly set in `local.properties`
  - **Check**: Look for "API key not configured" in logs
