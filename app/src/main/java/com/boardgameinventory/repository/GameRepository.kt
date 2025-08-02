package com.boardgameinventory.repository

import kotlinx.coroutines.flow.Flow
import com.boardgameinventory.data.Game
import com.boardgameinventory.data.GameDao
import com.boardgameinventory.api.ApiClient
import com.boardgameinventory.api.ProductInfo

class GameRepository(private val gameDao: GameDao) {
    
    fun getAllGames(): Flow<List<Game>> = gameDao.getAllGames()
    
    fun getLoanedGames(): Flow<List<Game>> = gameDao.getLoanedGames()
    
    fun getAvailableGames(): Flow<List<Game>> = gameDao.getAvailableGames()
    
    suspend fun getGameById(id: Long): Game? = gameDao.getGameById(id)
    
    suspend fun getGameByBarcode(barcode: String): Game? = gameDao.getGameByBarcode(barcode)
    
    suspend fun getAllBookcases(): List<String> = gameDao.getAllBookcases()
    
    suspend fun getShelvesForBookcase(bookcase: String): List<String> = 
        gameDao.getShelvesForBookcase(bookcase)
    
    suspend fun insertGame(game: Game): Long = gameDao.insertGame(game)
    
    suspend fun insertGames(games: List<Game>) = gameDao.insertGames(games)
    
    suspend fun updateGame(game: Game) = gameDao.updateGame(game)
    
    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)
    
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
}
