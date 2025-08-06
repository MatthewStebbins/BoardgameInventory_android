package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.api.ApiClient
import com.boardgameinventory.api.ProductInfo
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BulkUploadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = GameRepository(AppDatabase.getDatabase(application).gameDao())

    private val _scannedBarcodes = MutableLiveData<List<String>>(emptyList())
    val scannedBarcodes: LiveData<List<String>> = _scannedBarcodes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResult = MutableLiveData<UploadResult?>()
    val uploadResult: LiveData<UploadResult?> = _uploadResult

    fun addGameBarcode(barcode: String) {
        val currentList = _scannedBarcodes.value ?: emptyList()
        if (!currentList.contains(barcode)) {
            _scannedBarcodes.value = currentList + barcode
        }
    }

    fun removeGameBarcode(barcode: String) {
        val currentList = _scannedBarcodes.value ?: emptyList()
        _scannedBarcodes.value = currentList - barcode
    }

    fun uploadGames(bookcase: String, shelf: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val barcodes = _scannedBarcodes.value ?: emptyList()
                val successful = mutableListOf<String>()
                val failed = mutableListOf<String>()

                for (barcode in barcodes) {
                    try {
                        // Check if game already exists
                        val existingGame = repository.getGameByBarcode(barcode)

                        val game = if (existingGame != null) {
                            // Update existing game location
                            existingGame.copy(
                                bookcase = bookcase,
                                shelf = shelf
                            )
                        } else {
                            // Try to fetch from API
                            val gameData: ProductInfo? = try {
                                ApiClient.lookupBarcode(barcode)
                            } catch (e: Exception) {
                                null
                            }

                            Game(
                                barcode = barcode,
                                name = gameData?.getDisplayTitle() ?: "Unknown Game",
                                bookcase = bookcase,
                                shelf = shelf,
                                loanedTo = null,
                                description = gameData?.getDisplayDescription(),
                                imageUrl = gameData?.getDisplayImage()
                            )
                        }

                        repository.insertGame(game)
                        successful.add(barcode)
                    } catch (e: Exception) {
                        failed.add(barcode)
                    }
                }

                _uploadResult.value = UploadResult(successful.size, failed)
                _scannedBarcodes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearUploadResult() {
        _uploadResult.value = null
    }

    data class UploadResult(
        val successfulCount: Int,
        val failedBarcodes: List<String>
    )
}
