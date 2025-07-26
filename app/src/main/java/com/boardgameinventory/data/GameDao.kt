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
}
