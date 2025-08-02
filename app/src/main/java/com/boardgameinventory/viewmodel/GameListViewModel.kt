package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.data.SearchAndFilterCriteria
import com.boardgameinventory.data.SortCriteria
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GameListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GameRepository
    
    // Search and filter state
    private val _searchAndFilterCriteria = MutableStateFlow(SearchAndFilterCriteria())
    val searchAndFilterCriteria: StateFlow<SearchAndFilterCriteria> = _searchAndFilterCriteria.asStateFlow()
    
    // Filtered games
    val filteredAvailableGames: LiveData<List<Game>>
    val filteredLoanedGames: LiveData<List<Game>>
    
    // Filter options
    private val _availableBookcases = MutableStateFlow<List<String>>(emptyList())
    val availableBookcases: StateFlow<List<String>> = _availableBookcases.asStateFlow()
    
    private val _availableLocations = MutableStateFlow<List<String>>(emptyList())
    val availableLocations: StateFlow<List<String>> = _availableLocations.asStateFlow()
    
    // Original games (fallback)
    val availableGames: LiveData<List<Game>>
    val loanedGames: LiveData<List<Game>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        
        // Original games
        availableGames = repository.getAvailableGames().asLiveData()
        loanedGames = repository.getLoanedGames().asLiveData()
        
        // Filtered games based on search criteria - reactive to criteria changes
        filteredAvailableGames = _searchAndFilterCriteria
            .flatMapLatest { criteria ->
                repository.searchAndFilterAvailableGames(criteria)
            }
            .asLiveData()
            
        filteredLoanedGames = _searchAndFilterCriteria
            .flatMapLatest { criteria ->
                repository.searchAndFilterLoanedGames(criteria)
            }
            .asLiveData()
        
        // Load filter options
        loadFilterOptions()
    }
    
    fun updateSearchQuery(query: String) {
        _searchAndFilterCriteria.value = _searchAndFilterCriteria.value.copy(searchQuery = query)
    }
    
    fun updateBookcaseFilter(bookcase: String?) {
        _searchAndFilterCriteria.value = _searchAndFilterCriteria.value.copy(bookcaseFilter = bookcase)
    }
    
    fun updateDateFilter(fromDate: Long?, toDate: Long?) {
        _searchAndFilterCriteria.value = _searchAndFilterCriteria.value.copy(
            dateFromFilter = fromDate,
            dateToFilter = toDate
        )
    }
    
    fun updateSortCriteria(sortCriteria: SortCriteria) {
        _searchAndFilterCriteria.value = _searchAndFilterCriteria.value.copy(
            sortBy = sortCriteria
        )
    }
    
    fun clearFilters() {
        _searchAndFilterCriteria.value = SearchAndFilterCriteria()
    }
    
    fun getFilteredGames(type: String): Flow<List<Game>> {
        return when (type) {
            "available" -> repository.searchAndFilterAvailableGames(_searchAndFilterCriteria.value)
            "loaned" -> repository.searchAndFilterLoanedGames(_searchAndFilterCriteria.value)
            else -> repository.searchAndFilterGames(_searchAndFilterCriteria.value)
        }
    }
    
    private fun loadFilterOptions() {
        viewModelScope.launch {
            _availableBookcases.value = repository.getDistinctBookcases()
            _availableLocations.value = repository.getDistinctLocations()
        }
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
