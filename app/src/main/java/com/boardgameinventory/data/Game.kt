package com.boardgameinventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "games")
@Parcelize
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String,
    val bookcase: String,
    val shelf: String,
    val loanedTo: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateLoaned: Long? = null,
    val yearPublished: Int? = null,
    val category: String? = null,
    val tags: String? = null,
    val rating: Float? = null,
    val notes: String? = null,
    val playCount: Int? = null,
    val lastPlayed: Long? = null,
    val condition: String? = null,
    val purchasePrice: Float? = null,
    val purchaseDate: Long? = null,
    val retailer: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val playingTime: Int? = null,
    val minAge: Int? = null,
    val designer: String? = null,
    val publisher: String? = null,
    val bggId: Int? = null
) : Parcelable
