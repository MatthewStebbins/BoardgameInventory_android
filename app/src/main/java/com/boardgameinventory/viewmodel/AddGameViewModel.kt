package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.R
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddGameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GameRepository
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message
    
    private val _gameAdded = MutableStateFlow(false)
    val gameAdded: StateFlow<Boolean> = _gameAdded
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao(), application.applicationContext)
    }

    // Constructor that accepts a repository directly (used by ViewModelFactory)
    constructor(repository: GameRepository) : this(Application()) {
        // Repository is already set in the primary constructor
    }
    
    fun addGame(barcode: String, bookcase: String, shelf: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                // Check if game already exists
                val existingGame = repository.getGameByBarcode(barcode)
                if (existingGame != null) {
                    _message.value = "Game with this barcode already exists"
                    return@launch
                }
                
                val game = repository.addGameByBarcode(barcode, bookcase, shelf)
                
                if (game != null) {
                    _message.value = getApplication<Application>().getString(
                        R.string.game_added, 
                        game.name
                    )
                    _gameAdded.value = true
                } else {
                    _message.value = getApplication<Application>().getString(R.string.no_data_found)
                }
                
            } catch (e: Exception) {
                _message.value = "Error adding game: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
