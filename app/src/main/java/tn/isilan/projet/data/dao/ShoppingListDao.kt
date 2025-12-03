package tn.isilan.projet.data.dao

import androidx.room.*
import tn.isilan.projet.data.entities.ShoppingList
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingList>>

    @Insert
    suspend fun insertShoppingList(list: ShoppingList): Long

    @Delete
    suspend fun deleteShoppingList(list: ShoppingList)
}