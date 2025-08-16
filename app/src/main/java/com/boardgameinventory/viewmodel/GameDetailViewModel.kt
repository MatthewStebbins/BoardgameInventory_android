package com.boardgameinventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.launch

class GameDetailViewModel(private val repository: GameRepository) : ViewModel() {

    private var gameTitle: String = "Unknown Game"

    fun setGameTitle(title: String) {
        gameTitle = title
    }

    fun getGameTitle(): String {
        return gameTitle
    }

    fun syncGameData() {
        viewModelScope.launch {
            try {
                repository.syncGameData(gameTitle)
            } catch (e: Exception) {
                // Handle sync error
            }
        }
    }
}
