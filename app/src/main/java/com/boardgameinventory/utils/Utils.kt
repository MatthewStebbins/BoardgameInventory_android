package com.boardgameinventory.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    fun validateLocationBarcode(barcode: String): Pair<String?, String?> {
        if (barcode.isBlank() || !barcode.contains("-")) {
            return Pair(null, null)
        }
        
        val parts = barcode.split("-")
        if (parts.size != 2) {
            return Pair(null, null)
        }
        
        val bookcase = parts[0].trim()
        val shelf = parts[1].trim()
        
        if (bookcase.isBlank() || shelf.isBlank()) {
            return Pair(null, null)
        }
        
        return Pair(bookcase, shelf)
    }
    
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

}
