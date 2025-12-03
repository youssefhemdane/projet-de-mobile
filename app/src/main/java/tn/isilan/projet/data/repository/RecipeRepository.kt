package tn.isilan.projet.data.repository

import tn.isilan.projet.data.dao.RecipeDao
import tn.isilan.projet.data.dao.ShoppingListDao
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.entities.ShoppingList
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val shoppingListDao: ShoppingListDao
) {
    // Recipes
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)
    suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)
    fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)

    // Shopping Lists
    val allShoppingLists: Flow<List<ShoppingList>> = shoppingListDao.getAllShoppingLists()
    suspend fun insertShoppingList(list: ShoppingList): Long = shoppingListDao.insertShoppingList(list)
    suspend fun deleteShoppingList(list: ShoppingList) = shoppingListDao.deleteShoppingList(list)
}