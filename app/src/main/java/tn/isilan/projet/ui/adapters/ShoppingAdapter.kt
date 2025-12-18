package tn.isilan.projet.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tn.isilan.projet.R

// Modèle de données pour un item de la liste de courses
data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val quantity: String = "",
    val isChecked: Boolean = false
)

class ShoppingAdapter : RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder>() {

    private var items = mutableListOf<ShoppingItem>()
    private var onItemCheckedChange: ((ShoppingItem) -> Unit)? = null
    private var onItemDeleteClick: ((ShoppingItem) -> Unit)? = null

    class ShoppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxItem)
        val textItemName: TextView = itemView.findViewById(R.id.textItemName)
        val textQuantity: TextView = itemView.findViewById(R.id.textQuantity)
        val imageDelete: View = itemView.findViewById(R.id.imageDelete)
    }

    fun setOnItemCheckedChangeListener(listener: (ShoppingItem) -> Unit) {
        onItemCheckedChange = listener
    }

    fun setOnItemDeleteClickListener(listener: (ShoppingItem) -> Unit) {
        onItemDeleteClick = listener
    }

    fun updateItems(newItems: List<ShoppingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping, parent, false)
        return ShoppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val item = items[position]

        holder.textItemName.text = item.name
        holder.textQuantity.text = item.quantity
        holder.checkBox.isChecked = item.isChecked

        // Gérer le changement d'état de la checkbox
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val updatedItem = item.copy(isChecked = isChecked)
            items[position] = updatedItem
            onItemCheckedChange?.invoke(updatedItem)
        }

        // Gérer le clic sur le bouton supprimer
        holder.imageDelete.setOnClickListener {
            onItemDeleteClick?.invoke(item)
        }
    }

    override fun getItemCount() = items.size

    fun getItems() = items.toList()
}