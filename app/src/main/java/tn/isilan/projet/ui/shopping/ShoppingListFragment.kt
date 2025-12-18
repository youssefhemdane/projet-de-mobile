package tn.isilan.projet.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.entities.ShoppingItem
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentShoppingListBinding
import tn.isilan.projet.ui.adapters.ShoppingAdapter
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel
    private lateinit var shoppingAdapter: ShoppingAdapter

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
        val repository = RecipeRepository(database.recipeDao(), database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeRecipes()
    }

    private fun setupRecyclerView() {
        shoppingAdapter = ShoppingAdapter()

        // Configurer les listeners
        shoppingAdapter.onItemCheckedChange = { item ->
            // Mettre à jour l'item dans la base si nécessaire
            // item.isChecked est déjà mis à jour
        }

        shoppingAdapter.onItemDelete = { item ->
            shoppingAdapter.removeItem(item)
        }

        binding.recyclerViewShopping.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewShopping.adapter = shoppingAdapter
    }

    private fun setupButtons() {
        binding.buttonGenerateList.setOnClickListener {
            generateShoppingList()
        }

        binding.buttonClearChecked.setOnClickListener {
            clearCheckedItems()
        }
    }

    private fun clearCheckedItems() {
        val checkedItems = shoppingAdapter.getCheckedItems()
        checkedItems.forEach { item ->
            shoppingAdapter.removeItem(item)
        }
    }

    private fun observeRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes ->
                binding.textRecipeCount.text = "Nombre de recettes: ${recipes.size}"
            }
        }
    }

    private fun generateShoppingList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes ->
                if (recipes.isNotEmpty()) {
                    // Extraire tous les ingrédients
                    val allIngredients = recipes.flatMap { recipe ->
                        recipe.ingredients.split(",").map { it.trim() }
                    }

                    // Compter les occurrences
                    val ingredientCount = mutableMapOf<String, Int>()
                    allIngredients.forEach { ingredient ->
                        ingredientCount[ingredient] = ingredientCount.getOrDefault(ingredient, 0) + 1
                    }

                    // Créer les ShoppingItems
                    val shoppingItems = ingredientCount.map { (ingredient, count) ->
                        ShoppingItem(
                            name = ingredient,
                            quantity = if (count > 1) "$count recettes" else "1 recette",
                            isChecked = false
                        )
                    }.sortedBy { it.name }

                    // Mettre à jour l'adapter
                    shoppingAdapter.updateItems(shoppingItems)

                    // Garder l'ancien TextView pour référence
                    val shoppingText = shoppingItems.joinToString("\n") { item ->
                        "• ${item.name} (${item.quantity})"
                    }
                    binding.textShoppingList.text = shoppingText

                } else {
                    shoppingAdapter.updateItems(emptyList())
                    binding.textShoppingList.text = "Aucune recette disponible"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}