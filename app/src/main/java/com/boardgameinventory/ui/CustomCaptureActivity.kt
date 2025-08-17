package com.boardgameinventory.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import com.boardgameinventory.R
import com.google.android.material.button.MaterialButton
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * Custom capture activity to allow orientation changes for barcode scanning.
 * This ensures the camera preview matches device orientation.
 * Adds overlay controls for orientation lock and torch.
 */
class CustomCaptureActivity : CaptureActivity() {
    private var isTorchOn = false
    private var isOrientationLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ZXing will call initializeContent() after this, so overlay setup is done there
    }

    @SuppressLint("InflateParams")
    override fun initializeContent(): DecoratedBarcodeView {
        val barcodeView = super.initializeContent()
        // Inflate and add the overlay
        val overlay = LayoutInflater.from(this).inflate(R.layout.barcode_scanner_overlay, null)
        (barcodeView.parent as? android.widget.FrameLayout)?.addView(overlay)

        val btnTorch = overlay.findViewById<MaterialButton>(R.id.btn_toggle_torch)
        val btnOrientation = overlay.findViewById<MaterialButton>(R.id.btn_toggle_orientation)

        btnTorch.setOnClickListener {
            isTorchOn = !isTorchOn
            if (isTorchOn) {
                barcodeView.setTorchOn()
                btnTorch.setIconResource(R.drawable.ic_flashlight_off)
            } else {
                barcodeView.setTorchOff()
                btnTorch.setIconResource(R.drawable.ic_flashlight_on)
            }
        }

        btnOrientation.setOnClickListener {
            isOrientationLocked = !isOrientationLocked
            requestedOrientation = if (isOrientationLocked) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED
            } else {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            btnOrientation.setIconResource(
                if (isOrientationLocked) R.drawable.ic_screen_lock_rotation else R.drawable.ic_screen_rotation
            )
        }
        return barcodeView
    }
}
