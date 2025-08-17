package com.boardgameinventory.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ItemGameBinding

/**
 * Paging adapter for displaying games in a RecyclerView with pagination support
 * Extends PagingDataAdapter for efficient loading of large datasets
 */
class GamePagingAdapter(
    private val onItemAction: (Game, String) -> Unit
) : PagingDataAdapter<Game, GamePagingAdapter.GameViewHolder>(GameDiffCallback()) {
    
    private var deleteMode: Boolean = false
    private var showLoanedTo: Boolean = false
    
    companion object {
        const val ACTION_CLICK = "click"
        const val ACTION_LOAN = "loan"
        const val ACTION_RETURN = "return"
        const val ACTION_DELETE = "delete"
        const val ACTION_EDIT = "edit"
    }
    
    fun setDeleteMode(deleteMode: Boolean) {
        if (this.deleteMode != deleteMode) {
            this.deleteMode = deleteMode
            notifyItemRangeChanged(0, itemCount)
        }
    }

    fun setShowLoanedTo(showLoanedTo: Boolean) {
        if (this.showLoanedTo != showLoanedTo) {
            this.showLoanedTo = showLoanedTo
            notifyItemRangeChanged(0, itemCount)
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getItem(position)
        if (game != null) {
            holder.bind(game, deleteMode, showLoanedTo, onItemAction)
        }
    }
    
    class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            game: Game,
            deleteMode: Boolean,
            showLoanedTo: Boolean,
            onItemAction: (Game, String) -> Unit
        ) {
            binding.apply {
                // Basic game info
                tvGameName.text = game.name
                tvBarcode.text = "Barcode: ${game.barcode}"
                
                // Show loaned info if applicable
                if (game.loanedTo != null && showLoanedTo) {
                    tvLocation.text = "Loaned to: ${game.loanedTo}"
                    tvLoanStatus.visibility = View.VISIBLE
                    tvLoanStatus.text = "Loaned to ${game.loanedTo}"
                } else {
                    tvLocation.text = "Location: ${game.bookcase}, Shelf ${game.shelf}"
                    tvLoanStatus.visibility = View.GONE
                }
                
                // Load game image
                if (!game.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(game.imageUrl)
                        .placeholder(R.drawable.ic_game_placeholder)
                        .error(R.drawable.ic_game_placeholder)
                        .into(ivGameImage)
                } else {
                    ivGameImage.setImageResource(R.drawable.ic_game_placeholder)
                }
                
                // Click listener
                root.setOnClickListener {
                    onItemAction(game, ACTION_CLICK)
                }
                
                // Menu button
                btnMenu.setOnClickListener { view ->
                    showPopupMenu(view, game, deleteMode, onItemAction)
                }
            }
        }
        
        private fun showPopupMenu(
            view: View,
            game: Game,
            deleteMode: Boolean,
            onItemAction: (Game, String) -> Unit
        ) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.game_item_menu, popup.menu)
            
            // Show/hide menu items based on game state and mode
            popup.menu.findItem(R.id.action_loan)?.isVisible = !deleteMode && game.loanedTo.isNullOrBlank()
            popup.menu.findItem(R.id.action_return)?.isVisible = !deleteMode && !game.loanedTo.isNullOrBlank()
            popup.menu.findItem(R.id.action_edit)?.isVisible = !deleteMode
            popup.menu.findItem(R.id.action_delete)?.isVisible = true
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_loan -> {
                        onItemAction(game, ACTION_LOAN)
                        true
                    }
                    R.id.action_return -> {
                        onItemAction(game, ACTION_RETURN)
                        true
                    }
                    R.id.action_edit -> {
                        onItemAction(game, ACTION_EDIT)
                        true
                    }
                    R.id.action_delete -> {
                        onItemAction(game, ACTION_DELETE)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }
    
    private class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem == newItem
        }
    }
}
