package tn.isilan.projet.data.entities

data class ShoppingItem(
    val id: Int = 0,
    val name: String,
    val quantity: String = "",
    var isChecked: Boolean = false
)