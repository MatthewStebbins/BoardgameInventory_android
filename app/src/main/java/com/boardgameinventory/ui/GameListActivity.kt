package com.boardgameinventory.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boardgameinventory.R
import com.boardgameinventory.data.SearchAndFilterCriteria
import com.boardgameinventory.data.SortCriteria
import com.boardgameinventory.databinding.ActivityGameListBinding
import com.boardgameinventory.databinding.DialogFilterBinding
import com.boardgameinventory.viewmodel.GameListViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GameListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGameListBinding
    private val viewModel: GameListViewModel by viewModels()
    private var deleteMode = false
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        deleteMode = intent.getBooleanExtra("deleteMode", false)
        
        setupToolbar()
        setupViewPager()
        setupSearchAndFilter()
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (deleteMode) getString(R.string.delete_game) else getString(R.string.game_list_title)
        }
    }
    
    private fun setupViewPager() {
        val adapter = GameListPagerAdapter(this, deleteMode)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.available_games)
                1 -> getString(R.string.loaned_games)
                else -> ""
            }
        }.attach()
    }
    
    private fun setupSearchAndFilter() {
        // Setup search text input with debouncing to prevent freezing
        var searchJob: kotlinx.coroutines.Job? = null
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // Cancel previous search job to prevent multiple simultaneous searches
                searchJob?.cancel()
                
                searchJob = lifecycleScope.launch {
                    // Add debouncing to prevent excessive database queries
                    kotlinx.coroutines.delay(300) // Wait 300ms after user stops typing
                    viewModel.updateSearchQuery(s?.toString() ?: "")
                }
            }
        })
        
        // Setup filter button
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
        
        // Observe search criteria changes to update filter button
        lifecycleScope.launch {
            viewModel.searchAndFilterCriteria.collect { criteria ->
                updateFilterButtonState(criteria)
            }
        }
    }
    
    private fun updateFilterButtonState(criteria: SearchAndFilterCriteria) {
        val activeFilters = mutableListOf<String>()
        
        criteria.bookcaseFilter?.let { activeFilters.add("Bookcase") }
        if (criteria.dateFromFilter != null || criteria.dateToFilter != null) {
            activeFilters.add("Date")
        }
        
        // Update button appearance based on active filters
        val hasActiveFilters = activeFilters.isNotEmpty()
        binding.btnFilter.setIconTintResource(
            if (hasActiveFilters) android.R.color.holo_blue_dark 
            else android.R.color.darker_gray
        )
    }
    
    private fun showFilterDialog() {
        val dialogBinding = DialogFilterBinding.inflate(layoutInflater)
        
        // Setup spinners
        setupBookcaseSpinner(dialogBinding)
        
        // Setup date pickers
        setupDatePickers(dialogBinding)
        
        // Setup sort chips
        setupSortChips(dialogBinding)
        
        // Populate current values
        populateCurrentFilters(dialogBinding)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.applyFiltersButton.setOnClickListener {
            applyFilters(dialogBinding)
            dialog.dismiss()
        }
        
        dialogBinding.clearFiltersButton.setOnClickListener {
            viewModel.clearFilters()
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun setupBookcaseSpinner(dialogBinding: DialogFilterBinding) {
        lifecycleScope.launch {
            viewModel.availableBookcases.collect { bookcases ->
                val items = listOf(getString(R.string.all_bookcases)) + bookcases
                val adapter = ArrayAdapter(this@GameListActivity, android.R.layout.simple_dropdown_item_1line, items)
                dialogBinding.bookcaseSpinner.setAdapter(adapter)
            }
        }
    }
    
    private fun setupDatePickers(dialogBinding: DialogFilterBinding) {
        val calendar = Calendar.getInstance()
        
        dialogBinding.fromDateEditText.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                dialogBinding.fromDateEditText.setText(dateFormat.format(calendar.time))
                dialogBinding.fromDateEditText.tag = calendar.timeInMillis
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        dialogBinding.toDateEditText.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                dialogBinding.toDateEditText.setText(dateFormat.format(calendar.time))
                dialogBinding.toDateEditText.tag = calendar.timeInMillis
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    
    private fun setupSortChips(dialogBinding: DialogFilterBinding) {
        // Set up single selection for sort chips
        dialogBinding.sortChipGroup.isSingleSelection = true
        
        // Setup click listeners for sort chips
        dialogBinding.sortByNameChip.setOnClickListener {
            viewModel.updateSortCriteria(SortCriteria.NAME_ASC)
        }
        
        dialogBinding.sortByDateAddedChip.setOnClickListener {
            viewModel.updateSortCriteria(SortCriteria.DATE_ADDED_ASC)
        }
        
        dialogBinding.sortByBookcaseChip.setOnClickListener {
            viewModel.updateSortCriteria(SortCriteria.LOCATION_ASC)
        }
    }
    
    private fun populateCurrentFilters(dialogBinding: DialogFilterBinding) {
        val criteria = viewModel.searchAndFilterCriteria.value
        
        // Set bookcase
        criteria.bookcaseFilter?.let { bookcase ->
            dialogBinding.bookcaseSpinner.setText(bookcase, false)
        }
        
        // Set dates
        criteria.dateFromFilter?.let { timestamp ->
            val date = Date(timestamp)
            dialogBinding.fromDateEditText.setText(dateFormat.format(date))
            dialogBinding.fromDateEditText.tag = timestamp
        }
        
        criteria.dateToFilter?.let { timestamp ->
            val date = Date(timestamp)
            dialogBinding.toDateEditText.setText(dateFormat.format(date))
            dialogBinding.toDateEditText.tag = timestamp
        }
        
        // Set sort option
        val sortChipId = when (criteria.sortBy) {
            SortCriteria.NAME_ASC, SortCriteria.NAME_DESC -> R.id.sortByNameChip
            SortCriteria.DATE_ADDED_ASC, SortCriteria.DATE_ADDED_DESC -> R.id.sortByDateAddedChip
            SortCriteria.LOCATION_ASC, SortCriteria.LOCATION_DESC -> R.id.sortByBookcaseChip
        }
        
        dialogBinding.sortChipGroup.check(sortChipId)
    }
    
    private fun applyFilters(dialogBinding: DialogFilterBinding) {
        val bookcaseText = dialogBinding.bookcaseSpinner.text.toString()
        val bookcase = if (bookcaseText == getString(R.string.all_bookcases) || bookcaseText.isEmpty()) null else bookcaseText
        
        val fromDate = dialogBinding.fromDateEditText.tag as? Long
        val toDate = dialogBinding.toDateEditText.tag as? Long
        
        // Update filters to trigger the combined criteria update
        viewModel.updateBookcaseFilter(bookcase)
        viewModel.updateDateFilter(fromDate, toDate)
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_list_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_search -> {
                binding.etSearch.setText("")
                binding.etSearch.clearFocus()
                viewModel.clearFilters()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private class GameListPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val deleteMode: Boolean
    ) : FragmentStateAdapter(fragmentActivity) {
        
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> GameListFragment.newInstance(GameListFragment.TYPE_AVAILABLE, deleteMode)
                1 -> GameListFragment.newInstance(GameListFragment.TYPE_LOANED, deleteMode)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
