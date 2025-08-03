package com.boardgameinventory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boardgameinventory.databinding.ItemLoadingStateBinding

/**
 * LoadStateAdapter for showing loading states in paginated lists
 */
class GameLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<GameLoadStateAdapter.LoadStateViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadingStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding, retry)
    }
    
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    
    class LoadStateViewHolder(
        private val binding: ItemLoadingStateBinding,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(loadState: LoadState) {
            binding.apply {
                when (loadState) {
                    is LoadState.Loading -> {
                        progressBar.visibility = android.view.View.VISIBLE
                        textError.visibility = android.view.View.GONE
                        btnRetry.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        progressBar.visibility = android.view.View.GONE
                        textError.visibility = android.view.View.VISIBLE
                        textError.text = loadState.error.localizedMessage ?: "An error occurred"
                        btnRetry.visibility = android.view.View.VISIBLE
                        btnRetry.setOnClickListener { retry() }
                    }
                    is LoadState.NotLoading -> {
                        progressBar.visibility = android.view.View.GONE
                        textError.visibility = android.view.View.GONE
                        btnRetry.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}
