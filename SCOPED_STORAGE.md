# Scoped Storage Implementation Guide

## Overview
The BoardGame Inventory app now uses modern scoped storage APIs that comply with Android 10+ (API 29+) requirements, replacing deprecated external storage permissions.

## What Changed

### ❌ **Removed (Deprecated)**
- `WRITE_EXTERNAL_STORAGE` permission (deprecated on API 30+)
- `MANAGE_EXTERNAL_STORAGE` permission (commented out - only use if absolutely necessary)
- Direct file system access to external storage
- Legacy external storage APIs

### ✅ **Added (Modern)**
- Scoped storage with `MediaStore` API
- Storage Access Framework (SAF) integration
- App-private external storage (no permissions needed)
- Version-specific permission handling
- `ScopedStorageUtils` utility class

## Permission Strategy by Android Version

### Android 13+ (API 33+)
- **Granular media permissions**: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`
- **Use case**: Only if app needs to access user's media files
- **Our app**: Uses for potential future image handling

### Android 11-12 (API 30-32)
- **Single permission**: `READ_EXTERNAL_STORAGE`
- **Scoped storage**: Enforced by default
- **Our app**: Uses for compatibility with existing files

### Android 10 (API 29)
- **Legacy compatibility**: `requestLegacyExternalStorage="true"`
- **Scoped storage**: Optional but recommended
- **Our app**: Gradual migration to scoped storage

### Android 9 and below (API 28-)
- **Legacy permissions**: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
- **Full access**: Traditional external storage access
- **Our app**: Maintains backward compatibility

## Storage Locations

### 1. App-Private External Storage ✅ **Recommended**
```kotlin
// No permissions needed
val privateFile = ScopedStorageUtils.createPrivateFile(context, "games_backup.db")
```
- **Path**: `/Android/data/com.boardgameinventory/files/Documents/BoardGameInventory/`
- **Permissions**: None required
- **Persistence**: Deleted when app is uninstalled
- **Access**: Only by our app

### 2. Public Documents via MediaStore ✅ **For Sharing**
```kotlin
// Minimal permissions, uses MediaStore API
val publicUri = ScopedStorageUtils.createPublicDocument(context, "games_export.csv", "text/csv")
```
- **Path**: `/Documents/BoardGameInventory/`
- **Permissions**: Minimal (handled by MediaStore)
- **Persistence**: Survives app uninstall
- **Access**: Accessible by other apps

### 3. Storage Access Framework (SAF) ✅ **User Choice**
```kotlin
// No permissions needed, user chooses location
val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
```
- **Path**: User-selected location
- **Permissions**: None (user grants access)
- **Persistence**: User-controlled
- **Access**: User-controlled

## Implementation Details

### File Operations
```kotlin
// Create a file in app-private storage
val privateFile = ScopedStorageUtils.createPrivateFile(context, "backup.db")

// Create a file in public documents
val publicUri = ScopedStorageUtils.createPublicDocument(context, "export.csv", "text/csv")

// Write data to URI
ScopedStorageUtils.writeToUri(context, publicUri) { outputStream ->
    // Write your data here
}

// Read data from URI
ScopedStorageUtils.readFromUri(context, uri) { inputStream ->
    // Read your data here
}
```

### Permission Checking
```kotlin
// Check if permissions are needed for specific operation
val needsPermission = StoragePermissionUtils.needsStoragePermissions(
    context, 
    StoragePermissionUtils.StorageOperationType.PUBLIC_DOCUMENTS
)

// Get required permissions for current Android version
val permissions = StoragePermissionUtils.getRequiredStoragePermissions()
```

## Benefits of New Implementation

### 🔒 **Security**
- **Reduced permissions**: Only request what's actually needed
- **User control**: User chooses what files to access (SAF)
- **App isolation**: Private files are protected

### 📱 **Compatibility**
- **Play Store approved**: Complies with Google Play policies
- **Future-proof**: Works with Android 14+ requirements
- **Version adaptive**: Handles all Android versions appropriately

### 🚀 **Performance**
- **Faster access**: App-private storage is optimized
- **No permission prompts**: For most operations
- **Better UX**: Users aren't overwhelmed with permission requests

## Usage in App Features

### Database Backups
- **Location**: App-private external storage
- **Permissions**: None needed
- **Implementation**: `ScopedStorageUtils.createPrivateFile()`

### Data Export (CSV/Excel)
- **Location**: User-selected via SAF
- **Permissions**: None needed
- **Implementation**: `Intent.ACTION_CREATE_DOCUMENT`

### Data Import
- **Location**: User-selected files via SAF
- **Permissions**: None needed
- **Implementation**: `Intent.ACTION_OPEN_DOCUMENT`

### Game Images (Future)
- **Location**: App-private storage + MediaStore for public access
- **Permissions**: Granular media permissions if needed
- **Implementation**: `ScopedStorageUtils` + MediaStore API

## Migration Notes

### For Existing Users
1. **Automatic migration**: App will continue working
2. **Legacy support**: `requestLegacyExternalStorage` for Android 10
3. **Gradual transition**: New files use scoped storage

### For Developers
1. **Use `ScopedStorageUtils`**: For all new file operations
2. **Prefer private storage**: Unless file needs to persist after uninstall
3. **Use SAF for user files**: Let users choose location for exports/imports

## Testing Different Android Versions

### Test Matrix
- **Android 9 (API 28)**: Legacy permissions
- **Android 10 (API 29)**: Scoped storage with legacy fallback
- **Android 11 (API 30)**: Enforced scoped storage
- **Android 13 (API 33)**: Granular media permissions
- **Android 14+ (API 34+)**: Strictest scoped storage

### Test Scenarios
1. **Export games to CSV**: Should work without permissions
2. **Import games from file**: Should work without permissions
3. **Create database backup**: Should work without permissions
4. **Access existing files**: May require minimal permissions

## Troubleshooting

### Common Issues
1. **"Permission denied"**: Check if using correct storage location
2. **"File not found"**: Verify URI is valid and accessible
3. **"SecurityException"**: Ensure using scoped storage APIs

### Solutions
1. **Use app-private storage**: For app-specific data
2. **Use SAF**: For user-controlled file access
3. **Check Android version**: Apply appropriate permission strategy
