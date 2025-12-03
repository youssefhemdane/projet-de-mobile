package tn.isilan.projet.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentShoppingListBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser le ViewModel
        val database = RecipeDatabase.getInstance(requireContext())
        val repository = RecipeRepository(database.recipeDao() , database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(RecipeViewModel::class.java)

        setupGenerateButton()
        observeRecipes()
    }

    private fun setupGenerateButton() {
        binding.buttonGenerateList.setOnClickListener {
            generateShoppingList()
        }
    }

    private fun observeRecipes() {
        viewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            binding.textRecipeCount.text = "Nombre de recettes: ${recipes.size}"
        }
    }

    private fun generateShoppingList() {
        viewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isNotEmpty()) {
                val allIngredients = mutableListOf<String>()

                // Extraire tous les ingrédients de toutes les recettes
                recipes.forEach { recipe ->
                    val ingredientsList = recipe.ingredients.split(",").map { it.trim() }
                    allIngredients.addAll(ingredientsList)
                }

                // Supprimer les doublons et trier
                val uniqueIngredients = allIngredients.distinct().sorted()

                // Formater la liste
                val shoppingList = if (uniqueIngredients.isNotEmpty()) {
                    uniqueIngredients.joinToString("\n") { ingredient ->
                        "• $ingredient"
                    }
                } else {
                    "Aucun ingrédient trouvé dans vos recettes"
                }

                binding.textShoppingList.text = shoppingList
            } else {
                binding.textShoppingList.text = "Aucune recette disponible"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}