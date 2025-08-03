# AdMob Test Ads Troubleshooting Guide

## Issues: Not seeing test ads in the BoardGame Inventory app

### 🔧 **Immediate Troubleshooting Steps:**

## 1. **Check Emulator Internet Connection**
- Open Chrome or any browser on your emulator
- Try visiting google.com to confirm internet access
- If no internet: restart the emulator or check host network settings

## 2. **Verify Google Play Services**
- Open Google Play Store on emulator
- If it doesn't work or shows errors, you need an emulator with Google Play Services
- Use a "Google APIs" or "Google Play" system image

## 3. **Check App Logs (Most Important)**
When you open the app, look for these log messages in Android Studio's Logcat:

### **Expected AdMob Log Messages:**
```
D/AdManager: AdManager.initialize() called
D/AdManager: Initializing AdMob SDK...
D/AdManager: Debug build - setting up test device configuration
D/MainActivity: Found AdView in layout
D/MainActivity: AdView configured and ad loading started
D/AdManager: AdManager.loadAd() called
D/AdManager: Building ad request and loading ad...
D/AdManager: Ad request sent
D/MainActivity: Ad loaded successfully
```

### **Common Error Messages to Look For:**
```
E/Ads: No fill (means no ad available - normal in test mode sometimes)
E/MainActivity: Ad failed to load: [error message]
W/Ads: Not initialized (initialization problem)
```

## 4. **Filter Logcat for Ad Messages**
In Android Studio Logcat, use these filters:
- Tag: `AdManager`
- Tag: `MainActivity` 
- Tag: `Ads`
- Package: `com.google.android.gms.ads`

## 5. **Verify Test Ad Unit ID**
Our app is configured with Google's official test banner ad unit:
`ca-app-pub-3940256099942544/6300978111`

## 6. **Try Different Activities**
Test ads should appear on these screens:
- MainActivity (dashboard)
- GameListActivity (game list)
- AddGameActivity (add game form)
- GameDetailActivity (game details)
- EditGameActivity (edit game form)

## 🔍 **If Still No Ads Showing:**

### **Option A: Use Different Test Ad Unit**
Change the test ad unit in `AdManager.kt`:
```kotlin
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
```

### **Option B: Add Debug Ad Container**
Temporarily add a visible placeholder to see if the ad container is working:

1. In `layout_ad_banner.xml`, add this before the AdView:
```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="50dp"
    android:text="AD PLACEHOLDER - Should be replaced by banner"
    android:background="#FF0000"
    android:textColor="#FFFFFF"
    android:gravity="center"
    android:textSize="12sp" />
```

### **Option C: Enable Verbose AdMob Logging**
Add this to MainActivity onCreate():
```kotlin
MobileAds.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA)
```

## 📱 **Emulator Requirements:**
- **Must have Google Play Services installed**
- **Must have internet connection**
- Use API 28+ with Google Play Store support
- Recommended: Pixel 3a API 34 (which you're using ✓)

## 🎯 **Expected Behavior When Working:**
1. App loads normally
2. Small gray banner area appears at bottom of each screen
3. Test ad loads within 5-10 seconds
4. Ad shows "Test Ad" label or Google test content

## 🚨 **If Ads Still Don't Show:**
This might be due to:
1. **Google Play Services version** on emulator
2. **Regional restrictions** (rare for test ads)
3. **AdMob service availability** (temporary)

Try testing on a physical device if available - test ads are more reliable on real devices.

---

**Next Steps:** Launch the app, navigate to different screens, and check the logcat for the messages above. This will tell us exactly what's happening with the ad loading process.
