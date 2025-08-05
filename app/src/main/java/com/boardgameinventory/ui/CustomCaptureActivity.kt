package com.boardgameinventory.ui

import com.journeyapps.barcodescanner.CaptureActivity

/**
 * Custom capture activity to allow orientation changes for barcode scanning.
 * This ensures the camera preview matches device orientation.
 */
class CustomCaptureActivity : CaptureActivity()
