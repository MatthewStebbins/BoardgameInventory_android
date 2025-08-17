package com.boardgameinventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.launch

class GameDetailViewModel(private val repository: GameRepository) : ViewModel() {

    private var gameTitle: String = "Unknown Game"

    fun getGameTitle(): String {
        return gameTitle
    }

    fun syncGameData() {
        viewModelScope.launch {
            try {
                repository.syncGameData(gameTitle)
            } catch (_: Exception) {
                // Handle sync error
            }
        }
    }
}
