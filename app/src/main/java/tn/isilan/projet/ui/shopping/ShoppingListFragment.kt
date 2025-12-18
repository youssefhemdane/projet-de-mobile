package tn.isilan.projet.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentShoppingListBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupGenerateButton()
        observeRecipesCount()
    }

    private fun setupViewModel() {
        val database = RecipeDatabase.getInstance(requireContext())
        val repository =
            RecipeRepository(database.recipeDao(), database.shoppingListDao())
        val factory = ViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)
            .get(RecipeViewModel::class.java)
    }

    private fun setupGenerateButton() {
        binding.buttonGenerateList.setOnClickListener {
            generateShoppingList()
        }
    }

    private fun observeRecipesCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes: List<Recipe> ->
                binding.textRecipeCount.text =
                    "Nombre de recettes : ${recipes.size}"
            }
        }
    }

    private fun generateShoppingList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes: List<Recipe> ->

                if (recipes.isEmpty()) {
                    binding.textShoppingList.text =
                        "Aucune recette disponible"
                    return@collectLatest
                }

                val ingredients = recipes.flatMap { recipe ->
                    recipe.ingredients
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                }

                val uniqueIngredients = ingredients
                    .distinct()
                    .sorted()

                binding.textShoppingList.text =
                    if (uniqueIngredients.isNotEmpty()) {
                        uniqueIngredients.joinToString("\n") {
                            "• $it"
                        }
                    } else {
                        "Aucun ingrédient trouvé"
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
