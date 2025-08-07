package com.boardgameinventory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.boardgameinventory.databinding.ItemScannedBarcodeBinding

class ScannedBarcodesAdapter(
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<ScannedBarcodesAdapter.BarcodeViewHolder>() {

    private val barcodes = mutableListOf<String>()

    fun updateBarcodes(newBarcodes: List<String>) {
        val diffCallback = BarcodeDiffCallback(barcodes, newBarcodes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        barcodes.clear()
        barcodes.addAll(newBarcodes)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val binding = ItemScannedBarcodeBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return BarcodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {
        holder.bind(barcodes[position])
    }

    override fun getItemCount(): Int = barcodes.size

    inner class BarcodeViewHolder(private val binding: ItemScannedBarcodeBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(barcode: String) {
            binding.tvBarcode.text = barcode
            binding.btnRemove.setOnClickListener {
                onRemoveClick(barcode)
            }
        }
    }

    private class BarcodeDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
