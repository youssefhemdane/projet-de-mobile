package tn.isilan.projet.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val recipes: Flow<List<Recipe>> = repository.allRecipes

    fun addRecipe(
        title: String,
        description: String,
        ingredients: String,
        instructions: String,
        preparationTime: Int,
        difficulty: String
    ) {
        viewModelScope.launch {
            val recipe = Recipe(
                title = title,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = preparationTime,
                difficulty = difficulty
            )
            repository.insertRecipe(recipe)
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return repository.searchRecipes(query)
    }
}