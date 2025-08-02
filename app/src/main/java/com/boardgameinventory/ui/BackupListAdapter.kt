package com.boardgameinventory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boardgameinventory.data.BackupInfo
import com.boardgameinventory.databinding.ItemBackupBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying backup files
 */
class BackupListAdapter(
    private val onBackupClick: (BackupInfo) -> Unit
) : ListAdapter<BackupInfo, BackupListAdapter.BackupViewHolder>(BackupDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val binding = ItemBackupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BackupViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class BackupViewHolder(
        private val binding: ItemBackupBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(backupInfo: BackupInfo) {
            binding.apply {
                textFileName.text = backupInfo.fileName
                textFileSize.text = "${backupInfo.sizeBytes / 1024} KB"
                textCreatedDate.text = dateFormat.format(Date(backupInfo.createdDate))
                
                root.setOnClickListener {
                    onBackupClick(backupInfo)
                }
            }
        }
    }
}

/**
 * DiffUtil callback for efficient list updates
 */
class BackupDiffCallback : DiffUtil.ItemCallback<BackupInfo>() {
    override fun areItemsTheSame(oldItem: BackupInfo, newItem: BackupInfo): Boolean {
        return oldItem.fileName == newItem.fileName
    }
    
    override fun areContentsTheSame(oldItem: BackupInfo, newItem: BackupInfo): Boolean {
        return oldItem == newItem
    }
}
