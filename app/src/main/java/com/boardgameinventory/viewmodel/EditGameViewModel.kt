package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.R
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditGameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GameRepository
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message
    
    private val _gameUpdated = MutableStateFlow(false)
    val gameUpdated: StateFlow<Boolean> = _gameUpdated
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao(), application.applicationContext)
    }

    // Constructor that accepts a repository directly (used by ViewModelFactory)
    constructor(repository: GameRepository) : this(Application()) {
        // Repository is already set in the primary constructor
    }
    
    fun updateGame(game: Game) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                // Check if another game with the same barcode exists (excluding current game)
                val existingGame = repository.getGameByBarcode(game.barcode)
                if (existingGame != null && existingGame.id != game.id) {
                    _message.value = getApplication<Application>().getString(R.string.barcode_already_exists)
                    return@launch
                }
                
                repository.updateGame(game)
                
                _message.value = getApplication<Application>().getString(
                    R.string.game_updated_successfully, 
                    game.name
                )
                _gameUpdated.value = true
                
            } catch (e: Exception) {
                _message.value = getApplication<Application>().getString(
                    R.string.error_updating_game, 
                    e.message ?: "Unknown error"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
