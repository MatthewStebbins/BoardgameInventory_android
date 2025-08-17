package com.boardgameinventory.utils

import android.content.Context
import android.net.Uri
import com.boardgameinventory.data.Game
import com.opencsv.CSVReader
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStreamReader

object ImportUtils {
    // Parses a single CSV row, handling quoted fields and commas
    fun parseCSVRow(row: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < row.length) {
            val c = row[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < row.length && row[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    // Creates a Game object from CSV headers and values
    fun createGameFromCSVData(headers: List<String>, values: List<String>): Game? {
        if (headers.size != values.size) return null
        val map = headers.map { it.trim().lowercase() }.zip(values).toMap()
        return Game(
            name = map["name"] ?: map["title"] ?: "",
            barcode = map["barcode"] ?: "",
            bookcase = map["bookcase"] ?: "",
            shelf = map["shelf"] ?: "",
            loanedTo = map["loanedto"]?.takeIf { it.isNotBlank() },
            description = map["description"]?.takeIf { it.isNotBlank() },
            imageUrl = map["imageurl"]?.takeIf { it.isNotBlank() }
        )
    }

    // Checks if a game is a duplicate in a list
    fun isDuplicateGame(game: Game, existingGames: List<Game>): Boolean {
        return existingGames.any {
            it.name.equals(game.name, ignoreCase = true) &&
            it.barcode.equals(game.barcode, ignoreCase = true) &&
            it.bookcase.equals(game.bookcase, ignoreCase = true) &&
            it.shelf.equals(game.shelf, ignoreCase = true)
        }
    }

    // Parses a price string to a Double
    fun parsePrice(input: String): Double? {
        return input.replace("$", "").replace(",", "").toDoubleOrNull()
    }

    // Parses a date string in yyyy-MM-dd or MM/dd/yyyy format
    fun parseDate(input: String): String? {
        val trimmed = input.trim()
        return when {
            Regex("\\d{4}-\\d{2}-\\d{2}").matches(trimmed) -> trimmed
            Regex("\\d{2}/\\d{2}/\\d{4}").matches(trimmed) -> trimmed
            else -> null
        }
    }

    // Validates a game for import (must have title and publisher)
    fun validateGameForImport(game: Game): Boolean {
        return game.name.isNotBlank() && game.barcode.isNotBlank()
    }

    // Creates a map from headers and values
    fun createFieldMap(headers: List<String>, values: List<String>): Map<String, String> {
        return headers.zip(values).toMap()
    }

    // Checks if a file is an Excel file by extension
    fun isExcelFile(filename: String): Boolean {
        return filename.endsWith(".xlsx", true) || filename.endsWith(".xls", true)
    }

    fun parseCSVFromUri(context: Context, uri: Uri): List<Game> {
        val games = mutableListOf<Game>()
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                val lines = reader.readAll()
                
                if (lines.isNotEmpty()) {
                    // Skip header row
                    val dataLines = lines.drop(1)
                    
                    dataLines.forEach { line ->
                        if (line.size >= 4) { // Minimum required columns
                            val game = Game(
                                name = line.getOrNull(0) ?: "Unknown",
                                barcode = line.getOrNull(1) ?: "",
                                bookcase = line.getOrNull(2) ?: "",
                                shelf = line.getOrNull(3) ?: "",
                                loanedTo = line.getOrNull(4)?.takeIf { it.isNotBlank() },
                                description = line.getOrNull(5)?.takeIf { it.isNotBlank() },
                                imageUrl = line.getOrNull(6)?.takeIf { it.isNotBlank() }
                            )
                            if (game.name.isNotBlank() && game.barcode.isNotBlank()) {
                                games.add(game)
                            }
                        }
                    }
                }
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return games
    }
    
    fun parseExcelFromUri(context: Context, uri: Uri): List<Game> {
        val games = mutableListOf<Game>()
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                
                // Skip header row (row 0)
                for (rowIndex in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    
                    val name = row.getCell(0)?.toString()?.trim() ?: ""
                    val barcode = row.getCell(1)?.toString()?.trim() ?: ""
                    val bookcase = row.getCell(2)?.toString()?.trim() ?: ""
                    val shelf = row.getCell(3)?.toString()?.trim() ?: ""
                    val loanedTo = row.getCell(4)?.toString()?.trim()?.takeIf { it.isNotBlank() }
                    val description = row.getCell(5)?.toString()?.trim()?.takeIf { it.isNotBlank() }
                    val imageUrl = row.getCell(6)?.toString()?.trim()?.takeIf { it.isNotBlank() }
                    
                    if (name.isNotBlank() && barcode.isNotBlank()) {
                        val game = Game(
                            name = name,
                            barcode = barcode,
                            bookcase = bookcase,
                            shelf = shelf,
                            loanedTo = loanedTo,
                            description = description,
                            imageUrl = imageUrl
                        )
                        games.add(game)
                    }
                }
                workbook.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return games
    }
}
