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
    val dateLoaned: Long? = null
) : Parcelable
