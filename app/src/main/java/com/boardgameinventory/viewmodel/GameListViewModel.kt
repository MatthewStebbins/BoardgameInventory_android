package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GameListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GameRepository
    
    val availableGames: Flow<List<Game>>
    val loanedGames: Flow<List<Game>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        
        availableGames = repository.getAvailableGames()
        loanedGames = repository.getLoanedGames()
    }
    
    suspend fun loanGame(gameId: Long, person: String) {
        repository.loanGame(gameId, person)
    }
    
    suspend fun returnGame(gameId: Long) {
        repository.returnGame(gameId)
    }
    
    suspend fun deleteGame(gameId: Long) {
        repository.deleteGameById(gameId)
    }
    
    suspend fun updateGameLocation(gameId: Long, bookcase: String, shelf: String) {
        repository.updateGameLocation(gameId, bookcase, shelf)
    }
}
