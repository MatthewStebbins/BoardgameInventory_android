package com.boardgameinventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_entities")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val title: String?,
    val brand: String?,
    val description: String?,
    val imageUrl: String?,
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "loanedTo") val loanedTo: String? = null,
    @ColumnInfo(name = "dateAdded") val dateAdded: Long? = null,
    @ColumnInfo(name = "dateLoaned") val dateLoaned: Long? = null,
    @ColumnInfo(name = "bookcase") val bookcase: String? = null,
    @ColumnInfo(name = "shelf") val shelf: String? = null
)
