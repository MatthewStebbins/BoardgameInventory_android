package com.boardgameinventory.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.repository.GameRepository

/**
 * Factory for creating ViewModels with secure API repository injection
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val repository by lazy {
        val gameDao = AppDatabase.getDatabase(context).gameDao()
        GameRepository(gameDao, context)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                val viewModel = MainViewModel(repository)
                viewModel as T
            }
            modelClass.isAssignableFrom(GameListViewModel::class.java) -> {
                val viewModel = GameListViewModel(repository)
                viewModel as T
            }
            modelClass.isAssignableFrom(AddGameViewModel::class.java) -> {
                val viewModel = AddGameViewModel(repository)
                viewModel as T
            }
            modelClass.isAssignableFrom(EditGameViewModel::class.java) -> {
                val viewModel = EditGameViewModel(repository)
                viewModel as T
            }
            modelClass.isAssignableFrom(BulkUploadViewModel::class.java) -> {
                val viewModel = BulkUploadViewModel(repository, context)
                viewModel as T
            }
            modelClass.isAssignableFrom(ExportImportViewModel::class.java) -> {
                val viewModel = ExportImportViewModel(repository, context)
                viewModel as T
            }
            modelClass.isAssignableFrom(DatabaseManagementViewModel::class.java) -> {
                val viewModel = DatabaseManagementViewModel(context.applicationContext as Application)
                viewModel as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val instance = ViewModelFactory(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
