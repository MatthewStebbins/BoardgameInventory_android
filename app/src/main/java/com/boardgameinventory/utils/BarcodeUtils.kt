package com.boardgameinventory.utils

import com.journeyapps.barcodescanner.ScanOptions

/**
 * Utility class for configuring barcode scanning with phone orientation support.
 * Provides consistent scanner configuration across all activities in the app.
 */
object BarcodeUtils {
    
    /**
     * Creates optimized scan options for phone orientation scanning.
     * Allows users to rotate their phone for comfortable barcode scanning.
     * 
     * @param prompt Custom prompt text to display during scanning
     * @param enableBeep Whether to enable scan beep sound
     * @param saveImage Whether to save scanned barcode image (usually disabled for performance)
     * @return Configured ScanOptions instance
     */
    fun createPhoneOrientationScanOptions(
        prompt: String = "Position barcode in the frame to scan",
        enableBeep: Boolean = true,
        saveImage: Boolean = false,
        orientationLocked: Boolean = false,
        torchOn: Boolean = false
    ): ScanOptions {
        return ScanOptions().apply {
            setCaptureActivity(com.boardgameinventory.ui.CustomCaptureActivity::class.java)
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt(prompt)
            setCameraId(0)
            setBeepEnabled(enableBeep)
            setBarcodeImageEnabled(saveImage)
            setOrientationLocked(orientationLocked)
            setTimeout(30000)
            if (torchOn) setTorchEnabled(true)
        }
    }
    
    /**
     * Specific preset for game barcode scanning.
     * Optimized for scanning barcodes on board game boxes.
     */
    fun createGameBarcodeScanOptions(
        orientationLocked: Boolean = false,
        torchOn: Boolean = false
    ): ScanOptions {
        return createPhoneOrientationScanOptions(
            prompt = "📱 Rotate your phone for comfortable scanning\n\nPosition game barcode in the frame",
            enableBeep = true,
            saveImage = false,
            orientationLocked = orientationLocked,
            torchOn = torchOn
        )
    }
    
    /**
     * Specific preset for location barcode scanning.
     * Used for scanning location barcodes like "A-1", "B-2", etc.
     */
    fun createLocationBarcodeScanOptions(
        orientationLocked: Boolean = false,
        torchOn: Boolean = false
    ): ScanOptions {
        return createPhoneOrientationScanOptions(
            prompt = "📱 Rotate your phone for comfortable scanning\n\nScan Location Barcode (e.g., A-1)",
            enableBeep = true,
            saveImage = false,
            orientationLocked = orientationLocked,
            torchOn = torchOn
        )
    }
    
    /**
     * Specific preset for loan/return game scanning.
     * Used when scanning barcodes to loan or return games.
     */
    fun createLoanReturnScanOptions(
        isReturning: Boolean = false,
        orientationLocked: Boolean = false,
        torchOn: Boolean = false
    ): ScanOptions {
        val action = if (isReturning) "return" else "loan"
        return createPhoneOrientationScanOptions(
            prompt = "📱 Rotate your phone for comfortable scanning\n\nScan game barcode to $action",
            enableBeep = true,
            saveImage = false,
            orientationLocked = orientationLocked,
            torchOn = torchOn
        )
    }
    
    /**
     * High-speed scanning preset for bulk operations.
     * Optimized for rapid consecutive scans during bulk upload.
     */
    fun createBulkScanOptions(
        orientationLocked: Boolean = false,
        torchOn: Boolean = false
    ): ScanOptions {
        return createPhoneOrientationScanOptions(
            prompt = "📱 Bulk Scan Mode - Rotate for comfort\n\nScan next game barcode",
            enableBeep = true,
            saveImage = false,
            orientationLocked = orientationLocked,
            torchOn = torchOn
        ).apply {
            setTimeout(15000)
            setBarcodeImageEnabled(false)
        }
    }
}
