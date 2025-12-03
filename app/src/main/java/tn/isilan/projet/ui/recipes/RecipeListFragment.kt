package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import tn.isilan.projet.databinding.FragmentRecipeListBinding
import tn.isilan.projet.ui.adapters.RecipeAdapter
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory
// AJOUTÉ
import tn.isilan.projet.R

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser le ViewModel
        val database = tn.isilan.projet.data.database.RecipeDatabase.getInstance(requireContext())
        val repository = tn.isilan.projet.data.repository.RecipeRepository(database.recipeDao() , database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupRecyclerView()
        setupFab()
    }

    private fun setupRecyclerView() {
        val adapter = RecipeAdapter { recipe ->
            // Navigation avec Bundle (solution simple)
            val bundle = Bundle().apply {
                putLong("recipeId", recipe.id)
            }
            findNavController().navigate(
                R.id.action_recipeListFragment_to_recipeDetailFragment,
                bundle
            )
        }

        binding.recyclerViewRecipes.adapter = adapter
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())

        // Observer les recettes
        viewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
        }
    }

    private fun setupFab() {
        binding.fabAddRecipe.setOnClickListener {
            // Navigation simple
            findNavController().navigate(R.id.action_recipeListFragment_to_addRecipeFragment)
        }
    }

    private fun addSampleRecipes() {
        // Ajouter quelques recettes exemple si la base est vide
        viewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isEmpty()) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}