package com.boardgameinventory.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ItemGameBinding

class GameAdapter(
    private val onItemAction: (Game, String) -> Unit
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {
    
    companion object {
        const val ACTION_CLICK = "click"
        const val ACTION_LOAN = "loan"
        const val ACTION_RETURN = "return"
        const val ACTION_DELETE = "delete"
        const val ACTION_EDIT = "edit"
    }
    
    private var deleteMode = false
    private var showLoanedTo = false
    
    fun setDeleteMode(enabled: Boolean) {
        deleteMode = enabled
        notifyDataSetChanged()
    }
    
    fun setShowLoanedTo(show: Boolean) {
        showLoanedTo = show
        notifyDataSetChanged()
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
        holder.bind(getItem(position))
    }
    
    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(game: Game) {
            binding.apply {
                tvGameName.text = game.name
                tvBarcode.text = "Barcode: ${game.barcode}"
                
                if (game.loanedTo != null && showLoanedTo) {
                    tvLocation.text = "Loaned to: ${game.loanedTo}"
                    chipLoanStatus.visibility = View.VISIBLE
                    chipLoanStatus.text = "Loaned to ${game.loanedTo}"
                } else {
                    tvLocation.text = "Location: ${game.bookcase}, Shelf ${game.shelf}"
                    chipLoanStatus.visibility = View.GONE
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
                
                // Set click listeners
                root.setOnClickListener {
                    onItemAction(game, ACTION_CLICK)
                }
                
                btnMenu.setOnClickListener { view ->
                    showPopupMenu(view, game)
                }
            }
        }
        
        private fun showPopupMenu(view: View, game: Game) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.game_item_menu, popup.menu)
            
            // Configure menu items based on game state and mode
            val loanItem = popup.menu.findItem(R.id.action_loan)
            val returnItem = popup.menu.findItem(R.id.action_return)
            
            if (game.loanedTo != null) {
                loanItem.isVisible = false
                returnItem.isVisible = true
            } else {
                loanItem.isVisible = true
                returnItem.isVisible = false
            }
            
            if (deleteMode) {
                // In delete mode, only show delete option
                popup.menu.clear()
                popup.menu.add(0, R.id.action_delete, 0, "Delete")
            }
            
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
