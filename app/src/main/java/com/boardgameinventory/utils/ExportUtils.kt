package com.boardgameinventory.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.boardgameinventory.data.Game
import com.opencsv.CSVWriter
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*

object ExportUtils {
    
    fun exportToCSV(context: Context, games: List<Game>, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "games_export_${System.currentTimeMillis()}.csv")
        }
        launcher.launch(intent)
    }
    
    fun exportToExcel(context: Context, games: List<Game>, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, "games_export_${System.currentTimeMillis()}.xlsx")
        }
        launcher.launch(intent)
    }
    
    fun writeCSVToUri(context: Context, uri: Uri, games: List<Game>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = CSVWriter(OutputStreamWriter(outputStream))
                
                // Write header
                writer.writeNext(arrayOf("name", "barcode", "bookcase", "shelf", "loaned_to", "description", "image_url", "date_added"))
                
                // Write data
                games.forEach { game ->
                    writer.writeNext(arrayOf(
                        game.name,
                        game.barcode,
                        game.bookcase,
                        game.shelf,
                        game.loanedTo ?: "",
                        game.description ?: "",
                        game.imageUrl ?: "",
                        Utils.formatDate(game.dateAdded)
                    ))
                }
                
                writer.close()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun writeExcelToUri(context: Context, uri: Uri, games: List<Game>): Boolean {
        return try {
            val workbook: Workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Games")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("name", "barcode", "bookcase", "shelf", "loaned_to", "description", "image_url", "date_added")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            // Create data rows
            games.forEachIndexed { index, game ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(game.name)
                row.createCell(1).setCellValue(game.barcode)
                row.createCell(2).setCellValue(game.bookcase)
                row.createCell(3).setCellValue(game.shelf)
                row.createCell(4).setCellValue(game.loanedTo ?: "")
                row.createCell(5).setCellValue(game.description ?: "")
                row.createCell(6).setCellValue(game.imageUrl ?: "")
                row.createCell(7).setCellValue(Utils.formatDate(game.dateAdded))
            }
            
            // Auto-size columns
            headers.indices.forEach { sheet.autoSizeColumn(it) }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
                workbook.close()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
