package com.boardgameinventory.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.R
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.repository.GameRepository
import com.boardgameinventory.utils.ExportUtils
import com.boardgameinventory.utils.ImportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExportImportViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GameRepository
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message
    
    private val _exportSuccess = MutableStateFlow(false)
    val exportSuccess: StateFlow<Boolean> = _exportSuccess
    
    private val _importSuccess = MutableStateFlow<ImportResult?>(null)
    val importSuccess: StateFlow<ImportResult?> = _importSuccess
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
    }
    
    fun exportToCSV(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                val games = repository.getAllGamesOneTime()
                val success = ExportUtils.writeCSVToUri(getApplication(), uri, games)
                
                if (success) {
                    _message.value = getApplication<Application>().getString(
                        R.string.export_success_csv, 
                        games.size
                    )
                    _exportSuccess.value = true
                } else {
                    _message.value = getApplication<Application>().getString(R.string.export_error)
                }
                
            } catch (e: Exception) {
                _message.value = getApplication<Application>().getString(
                    R.string.export_error_detailed, 
                    e.message ?: "Unknown error"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun exportToExcel(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                val games = repository.getAllGamesOneTime()
                val success = ExportUtils.writeExcelToUri(getApplication(), uri, games)
                
                if (success) {
                    _message.value = getApplication<Application>().getString(
                        R.string.export_success_excel, 
                        games.size
                    )
                    _exportSuccess.value = true
                } else {
                    _message.value = getApplication<Application>().getString(R.string.export_error)
                }
                
            } catch (e: Exception) {
                _message.value = getApplication<Application>().getString(
                    R.string.export_error_detailed, 
                    e.message ?: "Unknown error"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun importFromCSV(uri: Uri, overwriteDatabase: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                val games = ImportUtils.parseCSVFromUri(getApplication(), uri)
                
                if (games.isEmpty()) {
                    _message.value = getApplication<Application>().getString(R.string.import_no_data)
                    return@launch
                }
                
                // Check for duplicate barcodes
                val existingBarcodes = repository.getAllBarcodes()
                val duplicates = games.filter { game -> 
                    existingBarcodes.contains(game.barcode) 
                }
                
                if (duplicates.isNotEmpty() && !overwriteDatabase) {
                    _message.value = getApplication<Application>().getString(
                        R.string.import_duplicates_found, 
                        duplicates.size
                    )
                    return@launch
                }
                
                if (overwriteDatabase) {
                    repository.deleteAllGames()
                }
                
                val importedCount = importGames(games, !overwriteDatabase)
                
                _message.value = getApplication<Application>().getString(
                    R.string.import_success_csv, 
                    importedCount
                )
                _importSuccess.value = ImportResult(importedCount, games.size - importedCount)
                
            } catch (e: Exception) {
                _message.value = getApplication<Application>().getString(
                    R.string.import_error_detailed, 
                    e.message ?: "Unknown error"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun importFromExcel(uri: Uri, overwriteDatabase: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                val games = ImportUtils.parseExcelFromUri(getApplication(), uri)
                
                if (games.isEmpty()) {
                    _message.value = getApplication<Application>().getString(R.string.import_no_data)
                    return@launch
                }
                
                // Check for duplicate barcodes
                val existingBarcodes = repository.getAllBarcodes()
                val duplicates = games.filter { game -> 
                    existingBarcodes.contains(game.barcode) 
                }
                
                if (duplicates.isNotEmpty() && !overwriteDatabase) {
                    _message.value = getApplication<Application>().getString(
                        R.string.import_duplicates_found, 
                        duplicates.size
                    )
                    return@launch
                }
                
                if (overwriteDatabase) {
                    repository.deleteAllGames()
                }
                
                val importedCount = importGames(games, !overwriteDatabase)
                
                _message.value = getApplication<Application>().getString(
                    R.string.import_success_excel, 
                    importedCount
                )
                _importSuccess.value = ImportResult(importedCount, games.size - importedCount)
                
            } catch (e: Exception) {
                _message.value = getApplication<Application>().getString(
                    R.string.import_error_detailed, 
                    e.message ?: "Unknown error"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun importGames(games: List<Game>, skipDuplicates: Boolean): Int {
        var importedCount = 0
        
        games.forEach { game ->
            try {
                if (skipDuplicates) {
                    val existingGame = repository.getGameByBarcode(game.barcode)
                    if (existingGame == null) {
                        repository.insertGame(game)
                        importedCount++
                    }
                } else {
                    repository.insertGame(game)
                    importedCount++
                }
            } catch (e: Exception) {
                // Continue with next game if one fails
            }
        }
        
        return importedCount
    }
    
    fun clearResults() {
        _exportSuccess.value = false
        _importSuccess.value = null
        _message.value = ""
    }
    
    data class ImportResult(
        val imported: Int,
        val skipped: Int
    )
}
