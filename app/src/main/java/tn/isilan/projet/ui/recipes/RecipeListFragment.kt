package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tn.isilan.projet.R
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentRecipeListBinding
import tn.isilan.projet.ui.adapters.RecipeAdapter
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory
import androidx.core.widget.doAfterTextChanged

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel
    private var sampleInserted = false

    // Adapters
    private lateinit var mainAdapter: RecipeAdapter
    private lateinit var searchAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val database = RecipeDatabase.getInstance(requireContext())
        val repository = RecipeRepository(database.recipeDao(), database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupAdapters()
        setupSearchView()
        setupRecyclerViews()
        setupFab()
        addSampleRecipes()
        observeRecipes()
    }

    private fun setupAdapters() {
        val onItemClick = { recipe: Recipe ->
            val bundle = Bundle().apply {
                putLong("recipeId", recipe.id)
            }
            findNavController().navigate(R.id.action_recipeList_to_recipeDetail, bundle)
        }

        mainAdapter = RecipeAdapter(onItemClick)
        searchAdapter = RecipeAdapter(onItemClick)
    }

    private fun setupSearchView() {
        // Setup SearchBar
        binding.searchBar.inflateMenu(R.menu.search_menu)

        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    binding.searchView.show()
                    true
                }
                else -> false
            }
        }

        // Setup SearchView
        binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.searchView.text.toString())
                binding.searchView.hide()
                true
            } else {
                false
            }
        }

        // Listen for text changes
        binding.searchView.editText.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
                showMainList()
            } else {
                performSearch(text.toString())
            }
        }

        // When search view closes, show main list
        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == com.google.android.material.search.SearchView.TransitionState.HIDING) {
                showMainList()
            }
        }
    }

    private fun setupRecyclerViews() {
        // Main RecyclerView
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = mainAdapter

        // Search Results RecyclerView
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            showMainList()
            return
        }

        // Show search results, hide main list
        binding.recyclerViewRecipes.visibility = View.GONE
        binding.searchResultsRecyclerView.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.searchRecipes(query).collectLatest { recipes ->
                searchAdapter.submitList(recipes)

                // Show empty state if no results
                if (recipes.isEmpty()) {
                    showEmptySearchState(query)
                }
            }
        }
    }

    private fun showEmptySearchState(query: String) {
        // You can add a TextView for empty state if you want
        // For now, just let the RecyclerView show empty
    }

    private fun showMainList() {
        binding.recyclerViewRecipes.visibility = View.VISIBLE
        binding.searchResultsRecyclerView.visibility = View.GONE
    }

    private fun observeRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes ->
                mainAdapter.submitList(recipes)
            }
        }
    }

    private fun setupFab() {
        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_recipeList_to_addRecipe)
        }
    }

    private fun addSampleRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes ->
                if (recipes.isEmpty() && !sampleInserted) {
                    sampleInserted = true

                    viewModel.addRecipe(
                        "Pâtes Carbonara",
                        "Un classique italien crémeux et délicieux",
                        "pâtes, œufs, lardons, parmesan, crème fraîche, poivre",
                        "1. Cuire les pâtes al dente\n2. Faire revenir les lardons\n3. Mélanger les œufs et la crème\n4. Tout mélanger et servir",
                        20,
                        "Facile"
                    )

                    viewModel.addRecipe(
                        "Salade César",
                        "Salade fraîche avec croûtons et parmesan",
                        "laitue, croûtons, parmesan, poulet, sauce césar",
                        "1. Laver et couper la laitue\n2. Griller le poulet\n3. Préparer la sauce\n4. Tout mélanger",
                        15,
                        "Facile"
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}