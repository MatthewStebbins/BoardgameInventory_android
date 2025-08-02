package com.boardgameinventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    
    @Query("SELECT * FROM games ORDER BY name ASC")
    fun getAllGames(): Flow<List<Game>>
    
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): Game?
    
    @Query("SELECT * FROM games WHERE barcode = :barcode")
    suspend fun getGameByBarcode(barcode: String): Game?
    
    @Query("SELECT * FROM games WHERE loanedTo IS NOT NULL ORDER BY dateLoaned DESC")
    fun getLoanedGames(): Flow<List<Game>>
    
    @Query("SELECT * FROM games WHERE loanedTo IS NULL ORDER BY name ASC")
    fun getAvailableGames(): Flow<List<Game>>
    
    @Query("SELECT DISTINCT bookcase FROM games ORDER BY bookcase ASC")
    suspend fun getAllBookcases(): List<String>
    
    @Query("SELECT DISTINCT shelf FROM games WHERE bookcase = :bookcase ORDER BY shelf ASC")
    suspend fun getShelvesForBookcase(bookcase: String): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<Game>)
    
    @Update
    suspend fun updateGame(game: Game)
    
    @Delete
    suspend fun deleteGame(game: Game)
    
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)
    
    @Query("DELETE FROM games")
    suspend fun deleteAllGames()
    
    @Query("UPDATE games SET loanedTo = :person, dateLoaned = :dateLoan WHERE id = :gameId")
    suspend fun loanGame(gameId: Long, person: String, dateLoan: Long)
    
    @Query("UPDATE games SET loanedTo = NULL, dateLoaned = NULL WHERE id = :gameId")
    suspend fun returnGame(gameId: Long)
    
    @Query("UPDATE games SET bookcase = :bookcase, shelf = :shelf WHERE id = :gameId")
    suspend fun updateGameLocation(gameId: Long, bookcase: String, shelf: String)
    
    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int
    
    @Query("SELECT COUNT(*) FROM games WHERE loanedTo IS NOT NULL")
    suspend fun getLoanedGameCount(): Int
    
    @Query("SELECT * FROM games ORDER BY name ASC")
    suspend fun getAllGamesOneTime(): List<Game>
    
    @Query("SELECT barcode FROM games")
    suspend fun getAllBarcodes(): List<String>
    
    // Search and Filter methods
    @Query("""
        SELECT * FROM games 
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR 
               name LIKE '%' || :searchQuery || '%' OR 
               barcode LIKE '%' || :searchQuery || '%' OR 
               description LIKE '%' || :searchQuery || '%')
        AND (:bookcase IS NULL OR bookcase = :bookcase)
        AND (:isLoaned IS NULL OR 
             (:isLoaned = 1 AND loanedTo IS NOT NULL) OR 
             (:isLoaned = 0 AND loanedTo IS NULL))
        AND (:dateFrom IS NULL OR dateAdded >= :dateFrom)
        AND (:dateTo IS NULL OR dateAdded <= :dateTo)
        ORDER BY 
            CASE WHEN :sortBy = 'NAME_ASC' THEN name END ASC,
            CASE WHEN :sortBy = 'NAME_DESC' THEN name END DESC,
            CASE WHEN :sortBy = 'DATE_ADDED_ASC' THEN dateAdded END ASC,
            CASE WHEN :sortBy = 'DATE_ADDED_DESC' THEN dateAdded END DESC,
            CASE WHEN :sortBy = 'LOCATION_ASC' THEN bookcase || shelf END ASC,
            CASE WHEN :sortBy = 'LOCATION_DESC' THEN bookcase || shelf END DESC
    """)
    fun searchAndFilterGames(
        searchQuery: String?,
        bookcase: String?,
        isLoaned: Int?, // 0 = available, 1 = loaned, null = all
        dateFrom: Long?,
        dateTo: Long?,
        sortBy: String
    ): Flow<List<Game>>
    
    @Query("SELECT DISTINCT bookcase FROM games WHERE bookcase IS NOT NULL AND bookcase != '' ORDER BY bookcase ASC")
    suspend fun getDistinctBookcases(): List<String>
    
    @Query("SELECT DISTINCT (bookcase || ' - ' || shelf) as location FROM games WHERE bookcase IS NOT NULL AND shelf IS NOT NULL ORDER BY location ASC")
    suspend fun getDistinctLocations(): List<String>
    
    // Health check queries
    @Query("SELECT COUNT(*) FROM games WHERE name IS NULL OR name = ''")
    suspend fun getGamesWithEmptyNames(): Int
    
    @Query("SELECT COUNT(*) FROM games WHERE barcode IS NULL OR barcode = ''")
    suspend fun getGamesWithEmptyBarcodes(): Int
}
