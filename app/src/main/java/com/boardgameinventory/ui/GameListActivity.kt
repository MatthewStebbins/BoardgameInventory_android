package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityGameListBinding
import com.google.android.material.tabs.TabLayoutMediator

class GameListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGameListBinding
    private var deleteMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        deleteMode = intent.getBooleanExtra("deleteMode", false)
        
        setupToolbar()
        setupViewPager()
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
