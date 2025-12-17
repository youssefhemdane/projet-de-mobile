package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel
    private var sampleInserted = false // avoid multiple inserts

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

        setupRecyclerView()
        setupFab()
        addSampleRecipes()
    }

    private fun setupRecyclerView() {
        val adapter = RecipeAdapter { recipe ->
            // Navigate with Bundle
            val bundle = Bundle().apply {
                putLong("recipeId", recipe.id)
            }
            findNavController().navigate(R.id.action_recipeList_to_recipeDetail, bundle)
        }

        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = adapter

        // Collect Flow from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes: List<Recipe> ->
                adapter.submitList(recipes)
            }
        }
    }

    private fun setupFab() {
        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_recipeList_to_addRecipe)
        }
    }

    private fun addSampleRecipes() {
        // Collect Flow to insert sample recipes if empty
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes: List<Recipe> ->
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
