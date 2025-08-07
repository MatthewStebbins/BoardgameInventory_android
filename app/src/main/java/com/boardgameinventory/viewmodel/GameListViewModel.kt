package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
class GameListViewModel : AndroidViewModel {

    private val repository: GameRepository

    // Search and filter state
    private val _searchAndFilterCriteria = MutableStateFlow(SearchAndFilterCriteria())
    val searchAndFilterCriteria: StateFlow<SearchAndFilterCriteria> = _searchAndFilterCriteria.asStateFlow()

    // Paginated games
    val pagedAvailableGames: Flow<PagingData<Game>>
    val pagedLoanedGames: Flow<PagingData<Game>>
    val pagedFilteredAvailableGames: Flow<PagingData<Game>>
    val pagedFilteredLoanedGames: Flow<PagingData<Game>>

    // Filter options
    private val _availableBookcases = MutableStateFlow<List<String>>(emptyList())
    val availableBookcases: StateFlow<List<String>> = _availableBookcases.asStateFlow()

    private val _availableLocations = MutableStateFlow<List<String>>(emptyList())
    val availableLocations: StateFlow<List<String>> = _availableLocations.asStateFlow()

    // Legacy support - non-paginated games (for backwards compatibility)
    val availableGames: LiveData<List<Game>>
    val loanedGames: LiveData<List<Game>>
    val filteredAvailableGames: LiveData<List<Game>>
    val filteredLoanedGames: LiveData<List<Game>>

    // Primary constructor with application parameter
    constructor(application: Application) : super(application) {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao(), application.applicationContext)

        // Initialize game data
        availableGames = repository.getAvailableGames().asLiveData()
        loanedGames = repository.getLoanedGames().asLiveData()

        pagedAvailableGames = repository.getAvailableGamesPaged().cachedIn(viewModelScope)
        pagedLoanedGames = repository.getLoanedGamesPaged().cachedIn(viewModelScope)

        filteredAvailableGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterAvailableGames(criteria)
        }.asLiveData()

        filteredLoanedGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterLoanedGames(criteria)
        }.asLiveData()

        pagedFilteredAvailableGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterAvailableGamesPaged(criteria)
        }.cachedIn(viewModelScope)

        pagedFilteredLoanedGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterLoanedGamesPaged(criteria)
        }.cachedIn(viewModelScope)

        loadFilterOptions()
    }

    // Secondary constructor with repository parameter for ViewModelFactory
    constructor(repository: GameRepository) : super(Application()) {
        this.repository = repository

        // Initialize game data
        availableGames = repository.getAvailableGames().asLiveData()
        loanedGames = repository.getLoanedGames().asLiveData()

        pagedAvailableGames = repository.getAvailableGamesPaged().cachedIn(viewModelScope)
        pagedLoanedGames = repository.getLoanedGamesPaged().cachedIn(viewModelScope)

        filteredAvailableGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterAvailableGames(criteria)
        }.asLiveData()

        filteredLoanedGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterLoanedGames(criteria)
        }.asLiveData()

        pagedFilteredAvailableGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterAvailableGamesPaged(criteria)
        }.cachedIn(viewModelScope)

        pagedFilteredLoanedGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterLoanedGamesPaged(criteria)
        }.cachedIn(viewModelScope)

        loadFilterOptions()
    }

    /**
     * Get paginated games by type
     * @param type "available", "loaned", or "all"
     * @param useFilter whether to apply current search/filter criteria
     */
    fun getPagedGames(type: String, useFilter: Boolean = false): Flow<PagingData<Game>> {
        return if (useFilter) {
            when (type) {
                "available" -> pagedFilteredAvailableGames
                "loaned" -> pagedFilteredLoanedGames
                else -> _searchAndFilterCriteria
                    .flatMapLatest { criteria ->
                        repository.searchAndFilterGamesPaged(criteria)
                    }
                    .cachedIn(viewModelScope)
            }
        } else {
            when (type) {
                "available" -> pagedAvailableGames
                "loaned" -> pagedLoanedGames
                else -> repository.getAllGamesPaged().cachedIn(viewModelScope)
            }
        }
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
