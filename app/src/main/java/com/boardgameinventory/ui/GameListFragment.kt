package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.FragmentGameListBinding
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.viewmodel.GameListViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class GameListFragment : Fragment() {
    
    companion object {
        const val TYPE_AVAILABLE = "available"
        const val TYPE_LOANED = "loaned"
        private const val ARG_TYPE = "type"
        private const val ARG_DELETE_MODE = "delete_mode"
        
        fun newInstance(type: String, deleteMode: Boolean = false): GameListFragment {
            return GameListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                    putBoolean(ARG_DELETE_MODE, deleteMode)
                }
            }
        }
    }
    
    private var _binding: FragmentGameListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: GameListViewModel by viewModels()
    private lateinit var adapter: GamePagingAdapter
    private lateinit var loadStateAdapter: GameLoadStateAdapter
    private var listType: String = TYPE_AVAILABLE
    private var deleteMode: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getString(ARG_TYPE, TYPE_AVAILABLE)
            deleteMode = it.getBoolean(ARG_DELETE_MODE, false)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeGames()
    }
    
    private fun setupRecyclerView() {
        adapter = GamePagingAdapter { game, action ->
            when (action) {
                GamePagingAdapter.ACTION_CLICK -> {
                    if (deleteMode) {
                        showDeleteConfirmation(game)
                    } else {
                        openGameDetail(game)
                    }
                }
                GamePagingAdapter.ACTION_LOAN -> loanGame(game)
                GamePagingAdapter.ACTION_RETURN -> returnGame(game)
                GamePagingAdapter.ACTION_DELETE -> showDeleteConfirmation(game)
                GamePagingAdapter.ACTION_EDIT -> editGame(game)
            }
        }
        
        loadStateAdapter = GameLoadStateAdapter { adapter.retry() }
        
        adapter.setDeleteMode(deleteMode)
        adapter.setShowLoanedTo(listType == TYPE_LOANED)
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GameListFragment.adapter.withLoadStateFooter(loadStateAdapter)
        }
    }
    
    private fun observeGames() {
        // Observe paginated games using lifecycle scope
        lifecycleScope.launch {
            if (listType == TYPE_AVAILABLE) {
                viewModel.pagedAvailableGames.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            } else {
                viewModel.pagedLoanedGames.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }
    
    private fun openGameDetail(game: Game) {
        val intent = Intent(context, GameDetailActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
    }
    
    private fun loanGame(game: Game) {
        val intent = Intent(context, LoanGameActivity::class.java)
        intent.putExtra("gameId", game.id)
        startActivity(intent)
    }
    
    private fun returnGame(game: Game) {
        lifecycleScope.launch {
            viewModel.returnGame(game.id)
            Utils.showToast(requireContext(), "Game returned")
        }
    }
    
    private fun editGame(game: Game) {
        val intent = Intent(context, EditGameActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
    }
    
    private fun showDeleteConfirmation(game: Game) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm")
            .setMessage("Delete ${game.name}?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteGame(game)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun deleteGame(game: Game) {
        lifecycleScope.launch {
            viewModel.deleteGame(game.id)
            Utils.showToast(requireContext(), "Game deleted")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
