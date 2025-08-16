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

/**
 * Main ViewModel supporting secure API key access
 */
class MainViewModel : AndroidViewModel {

    private val repository: GameRepository

    private val _gameStats = MutableStateFlow(GameStats())
    val gameStats: StateFlow<GameStats> = _gameStats

    // Primary constructor with application
    constructor(application: Application) : super(application) {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        refreshStats()
    }

    // Secondary constructor with repository for ViewModelFactory
    constructor(repository: GameRepository) : super(Application()) {
        this.repository = repository
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
