package com.boardgameinventory.repository

import kotlinx.coroutines.flow.Flow
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.boardgameinventory.BuildConfig
import com.boardgameinventory.data.Game
import com.boardgameinventory.data.GameDao
import com.boardgameinventory.data.SearchAndFilterCriteria
import com.boardgameinventory.api.ApiClient
import com.boardgameinventory.api.ProductInfo // Import the ProductInfo class
import androidx.paging.PagingSource

class GameRepository(private val gameDao: GameDao) { // Removed unused 'context' parameter

    fun getLoanedGames(): Flow<List<Game>> = gameDao.getLoanedGames()
    
    fun getAvailableGames(): Flow<List<Game>> = gameDao.getAvailableGames()

    suspend fun getGameByBarcode(barcode: String): Game? = gameDao.getGameByBarcode(barcode)
    
    suspend fun insertGame(game: Game): Long = gameDao.insertGame(game)
    
    suspend fun updateGame(game: Game) = gameDao.updateGame(game)
    
    suspend fun deleteGameById(id: Long) = gameDao.deleteGameById(id)
    
    suspend fun deleteAllGames() = gameDao.deleteAllGames()
    
    suspend fun loanGame(gameId: Long, person: String) = 
        gameDao.loanGame(gameId, person, System.currentTimeMillis())
    
    suspend fun returnGame(gameId: Long) = gameDao.returnGame(gameId)
    
    suspend fun updateGameLocation(gameId: Long, bookcase: String, shelf: String) = 
        gameDao.updateGameLocation(gameId, bookcase, shelf)
    
    suspend fun getGameCount(): Int = gameDao.getGameCount()
    
    suspend fun getLoanedGameCount(): Int = gameDao.getLoanedGameCount()
    
    suspend fun getAllGamesOneTime(): List<Game> = gameDao.getAllGamesOneTime()
    
    suspend fun getAllBarcodes(): List<String> = gameDao.getAllBarcodes()
    
    suspend fun lookupBarcodeInfo(barcode: String): ProductInfo? = ApiClient.lookupBarcode(barcode)

    suspend fun addGameByBarcode(
        barcode: String, 
        bookcase: String, 
        shelf: String
    ): Game? {
        // Check if game already exists
        val existingGame = getGameByBarcode(barcode)
        if (existingGame != null) {
            return existingGame
        }
        
        // Look up barcode info
        val productInfo = lookupBarcodeInfo(barcode)
        if (BuildConfig.DEBUG) {
            android.util.Log.d("GameRepository", "ProductInfo from API: $productInfo")
            if (productInfo == null) {
                android.util.Log.w("GameRepository", "No product found for barcode: $barcode")
            }
        }
        val name = productInfo?.getDisplayTitle() ?: "Unknown Game"
        val description = productInfo?.getDisplayDescription()
        val imageUrl = productInfo?.getDisplayImage()
        
        val game = Game(
            name = name,
            barcode = barcode,
            bookcase = bookcase,
            shelf = shelf,
            description = description,
            imageUrl = imageUrl
        )
        
        val id = insertGame(game)
        return game.copy(id = id)
    }
    
    // Search and Filter methods
    fun searchAndFilterGames(criteria: SearchAndFilterCriteria): Flow<List<Game>> {
        // Only perform search if query is empty or has at least 2 characters
        val searchQuery = if (criteria.searchQuery.isBlank() || criteria.searchQuery.length < 2) {
            null
        } else {
            criteria.searchQuery
        }
        
        val isLoaned = when {
            criteria.searchQuery.contains("available", ignoreCase = true) -> 0
            criteria.searchQuery.contains("loaned", ignoreCase = true) -> 1
            else -> null
        }
        
        return gameDao.searchAndFilterGames(
            searchQuery = searchQuery,
            bookcase = criteria.bookcaseFilter,
            isLoaned = isLoaned,
            dateFrom = criteria.dateFromFilter,
            dateTo = criteria.dateToFilter,
            sortBy = criteria.sortBy.name
        )
    }
    
    fun searchAndFilterAvailableGames(criteria: SearchAndFilterCriteria): Flow<List<Game>> {
        // Only perform search if query is empty or has at least 2 characters
        val searchQuery = if (criteria.searchQuery.isBlank() || criteria.searchQuery.length < 2) {
            null
        } else {
            criteria.searchQuery
        }
        
        return gameDao.searchAndFilterGames(
            searchQuery = searchQuery,
            bookcase = criteria.bookcaseFilter,
            isLoaned = 0, // Only available games
            dateFrom = criteria.dateFromFilter,
            dateTo = criteria.dateToFilter,
            sortBy = criteria.sortBy.name
        )
    }
    
    fun searchAndFilterLoanedGames(criteria: SearchAndFilterCriteria): Flow<List<Game>> {
        // Only perform search if query is empty or has at least 2 characters
        val searchQuery = if (criteria.searchQuery.isBlank() || criteria.searchQuery.length < 2) {
            null
        } else {
            criteria.searchQuery
        }
        
        return gameDao.searchAndFilterGames(
            searchQuery = searchQuery,
            bookcase = criteria.bookcaseFilter,
            isLoaned = 1, // Only loaned games
            dateFrom = criteria.dateFromFilter,
            dateTo = criteria.dateToFilter,
            sortBy = criteria.sortBy.name
        )
    }
    
    suspend fun getDistinctBookcases(): List<String> = gameDao.getDistinctBookcases()
    
    suspend fun getDistinctLocations(): List<String> = gameDao.getDistinctLocations()
    
    // Pagination methods
    companion object {
        const val PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
        const val INITIAL_LOAD_SIZE = 40
    }
    
    /**
     * Get all games with pagination
     */
    fun getAllGamesPaged(): Flow<PagingData<Game>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { gameDao.getAllGamesPaged() }
        ).flow
    }
    
    /**
     * Get available games with pagination
     */
    fun getAvailableGamesPaged(): Flow<PagingData<Game>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { gameDao.getAvailableGamesPaged() }
        ).flow
    }
    
    /**
     * Get loaned games with pagination
     */
    fun getLoanedGamesPaged(): Flow<PagingData<Game>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { gameDao.getLoanedGamesPaged() }
        ).flow
    }
    
    /**
     * Search and filter all games with pagination
     */
    fun searchAndFilterGamesPaged(criteria: SearchAndFilterCriteria): Flow<PagingData<Game>> {
        val searchQuery = if (criteria.searchQuery.isBlank() || criteria.searchQuery.length < 2) {
            null
        } else {
            criteria.searchQuery
        }
        
        val isLoaned = when {
            criteria.searchQuery.contains("available", ignoreCase = true) -> 0
            criteria.searchQuery.contains("loaned", ignoreCase = true) -> 1
            else -> null
        }
        
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                gameDao.searchAndFilterGamesPaged(
                    searchQuery = searchQuery,
                    bookcase = criteria.bookcaseFilter,
                    isLoaned = isLoaned,
                    dateFrom = criteria.dateFromFilter,
                    dateTo = criteria.dateToFilter,
                    sortBy = criteria.sortBy.name
                )
            }
        ).flow
    }
    
    /**
     * Search and filter available games with pagination
     */
    fun searchAndFilterAvailableGamesPaged(criteria: SearchAndFilterCriteria): Flow<PagingData<Game>> {
        val searchQuery = if (criteria.searchQuery.isBlank() || criteria.searchQuery.length < 2) {
            null
        } else {
            criteria.searchQuery
        }
        
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                gameDao.searchAndFilterAvailableGamesPaged(
                    searchQuery = searchQuery,
                    bookcase = criteria.bookcaseFilter,
                    dateFrom = criteria.dateFromFilter,
                    dateTo = criteria.dateToFilter,
                    sortBy = criteria.sortBy.name
                )
            }
        ).flow
    }
    
    /**
     * Search and filter loaned games with pagination
     */
    fun searchAndFilterLoanedGamesPaged(criteria: SearchAndFilterCriteria): Flow<PagingData<Game>> {
        val searchQuery = if (criteria.searchQuery.isBlank() || criteria.searchQuery.length < 2) {
            null
        } else {
            criteria.searchQuery
        }
        
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                gameDao.searchAndFilterLoanedGamesPaged(
                    searchQuery = searchQuery,
                    bookcase = criteria.bookcaseFilter,
                    dateFrom = criteria.dateFromFilter,
                    dateTo = criteria.dateToFilter,
                    sortBy = criteria.sortBy.name
                )
            }
        ).flow
    }

    fun getAvailableGamesPaging(): Flow<PagingData<Game>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { gameDao.getAvailableGamesPagingSource() }
    ).flow

    fun getLoanedGamesPaging(): Flow<PagingData<Game>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { gameDao.getLoanedGamesPagingSource() }
    ).flow

    fun getFilteredAvailableGamesPaging(criteria: SearchAndFilterCriteria): Flow<PagingData<Game>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { gameDao.getFilteredAvailableGamesPagingSource(criteria.searchQuery) }
    ).flow

    fun getFilteredLoanedGamesPaging(criteria: SearchAndFilterCriteria): Flow<PagingData<Game>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { gameDao.getFilteredLoanedGamesPagingSource(criteria.searchQuery) }
    ).flow

    fun getFilteredAvailableGames(criteria: SearchAndFilterCriteria): Flow<List<Game>> =
        gameDao.getFilteredAvailableGames(criteria.searchQuery)

    fun getFilteredLoanedGames(criteria: SearchAndFilterCriteria): Flow<List<Game>> =
        gameDao.getFilteredLoanedGames(criteria.searchQuery)

    suspend fun syncGameData(gameTitle: String) {
        if (gameTitle.isNotBlank()) {
            val productInfo = ApiClient.lookupBarcode(gameTitle)
            productInfo?.let { info ->
                val game = Game(
                    name = info.name ?: info.title ?: info.productName ?: "Unknown Game",
                    barcode = info.barcode ?: "",
                    bookcase = "Default Bookcase", // Default values can be adjusted
                    shelf = "Default Shelf",
                    description = info.description ?: info.desc,
                    imageUrl = null // No imageUrl property exists in ProductInfo
                )
                gameDao.updateGame(game)
            }
        }
    }
}
