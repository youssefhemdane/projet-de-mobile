package tn.isilan.projet.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.isilan.projet.data.entities.ShoppingList
import tn.isilan.projet.data.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ShoppingViewModel(private val repository: RecipeRepository) : ViewModel() {

    val shoppingLists: Flow<List<ShoppingList>> = repository.allShoppingLists

    fun addShoppingList(name: String, items: String) {
        viewModelScope.launch {
            val list = ShoppingList(name = name, items = items)
            repository.insertShoppingList(list)
        }
    }

    fun deleteShoppingList(list: ShoppingList) {
        viewModelScope.launch {
            repository.deleteShoppingList(list)
        }
    }
}