package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GameStats(
    val totalGames: Int = 0,
    val loanedGames: Int = 0,
    val availableGames: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GameRepository
    
    private val _gameStats = MutableStateFlow(GameStats())
    val gameStats: StateFlow<GameStats> = _gameStats
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        refreshStats()
    }
    
    fun refreshStats() {
        viewModelScope.launch {
            val totalGames = repository.getGameCount()
            val loanedGames = repository.getLoanedGameCount()
            val availableGames = totalGames - loanedGames
            
            _gameStats.value = GameStats(
                totalGames = totalGames,
                loanedGames = loanedGames,
                availableGames = availableGames
            )
        }
    }
}
