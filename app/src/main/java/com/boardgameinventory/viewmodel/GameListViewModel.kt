package com.boardgameinventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.boardgameinventory.data.Game
import com.boardgameinventory.data.SearchAndFilterCriteria
import com.boardgameinventory.data.SortCriteria
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GameListViewModel(private val repository: GameRepository) : ViewModel() {

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

    // Legacy support - non-paginated games (for backwards compatibility)
    val availableGames: LiveData<List<Game>>
    val loanedGames: LiveData<List<Game>>
    val filteredAvailableGames: LiveData<List<Game>>
    val filteredLoanedGames: LiveData<List<Game>>

    // Selected game and validation error (for LoanGameActivity)
    val selectedGame = MutableLiveData<Game?>()
    val validationError = MutableLiveData<String>()

    init {
        // Initialize paged games using repository
        pagedAvailableGames = repository.getAvailableGamesPaged().cachedIn(viewModelScope)
        pagedLoanedGames = repository.getLoanedGamesPaged().cachedIn(viewModelScope)
        pagedFilteredAvailableGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterAvailableGamesPaged(criteria)
        }.cachedIn(viewModelScope)
        pagedFilteredLoanedGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterLoanedGamesPaged(criteria)
        }.cachedIn(viewModelScope)

        // Initialize legacy support games
        availableGames = repository.getAvailableGames().asLiveData()
        loanedGames = repository.getLoanedGames().asLiveData()
        filteredAvailableGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterAvailableGames(criteria)
        }.asLiveData()
        filteredLoanedGames = _searchAndFilterCriteria.flatMapLatest { criteria ->
            repository.searchAndFilterLoanedGames(criteria)
        }.asLiveData()

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
