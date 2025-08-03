package com.boardgameinventory.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.boardgameinventory.data.Game
import com.opencsv.CSVReader
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStreamReader

object ImportUtils {
    
    fun importFromCSV(context: Context, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
        }
        launcher.launch(intent)
    }
    
    fun importFromExcel(context: Context, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        launcher.launch(intent)
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
