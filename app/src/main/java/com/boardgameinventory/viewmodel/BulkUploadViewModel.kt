package com.boardgameinventory.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
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

    private val repository: GameRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
    }

    // Constructor that accepts a repository directly (used by ViewModelFactory)
    constructor(repository: GameRepository, context: Context) : this(context.applicationContext as Application) {
        // Repository is already set in the primary constructor
    }

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
            Log.d("BulkUploadViewModel", "Starting uploadGames with barcodes: ${_scannedBarcodes.value}")

            try {
                val barcodes = _scannedBarcodes.value ?: emptyList()
                val (successful, failed) = processBarcodes(barcodes, bookcase, shelf)

                Log.d("BulkUploadViewModel", "Upload result - Successful: $successful, Failed: $failed")

                _uploadResult.value = UploadResult(successful.size, failed)
                _scannedBarcodes.value = emptyList()
            } catch (e: Exception) {
                Log.e("BulkUploadViewModel", "Error during uploadGames", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun processBarcodes(
        barcodes: List<String>,
        bookcase: String,
        shelf: String
    ): Pair<List<String>, List<String>> {
        val successful = mutableListOf<String>()
        val failed = mutableListOf<String>()

        for (barcode in barcodes) {
            try {
                val game = getOrCreateGame(barcode, bookcase, shelf)
                repository.insertGame(game)
                successful.add(barcode)
            } catch (e: Exception) {
                failed.add(barcode)
            }
        }

        return Pair(successful, failed)
    }

    private suspend fun getOrCreateGame(
        barcode: String,
        bookcase: String,
        shelf: String
    ): Game {
        val existingGame = repository.getGameByBarcode(barcode)

        return if (existingGame != null) {
            existingGame.copy(bookcase = bookcase, shelf = shelf)
        } else {
            fetchGameFromApi(barcode, bookcase, shelf)
        }
    }

    private suspend fun fetchGameFromApi(
        barcode: String,
        bookcase: String,
        shelf: String
    ): Game {
        val gameData: ProductInfo? = try {
            ApiClient.lookupBarcode(barcode)
        } catch (e: Exception) {
            null
        }

        return Game(
            barcode = barcode,
            name = gameData?.getDisplayTitle() ?: "Unknown Game",
            bookcase = bookcase,
            shelf = shelf,
            loanedTo = null,
            description = gameData?.getDisplayDescription(),
            imageUrl = gameData?.getDisplayImage()
        )
    }

    fun clearUploadResult() {
        _uploadResult.value = null
    }

    data class UploadResult(
        val successfulCount: Int,
        val failedBarcodes: List<String>
    )
}
