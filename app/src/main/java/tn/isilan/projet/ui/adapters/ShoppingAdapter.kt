package tn.isilan.projet.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tn.isilan.projet.R
import tn.isilan.projet.data.entities.ShoppingItem

class ShoppingAdapter : RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder>() {

    private var shoppingItems = mutableListOf<ShoppingItem>()

    // Listeners pour les événements
    var onItemCheckedChange: ((ShoppingItem) -> Unit)? = null
    var onItemDelete: ((ShoppingItem) -> Unit)? = null

    class ShoppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxItem)
        val textItemName: TextView = itemView.findViewById(R.id.textItemName)
        val textQuantity: TextView = itemView.findViewById(R.id.textQuantity)
        val imageDelete: ImageView = itemView.findViewById(R.id.imageDelete)
    }

    fun updateItems(newItems: List<ShoppingItem>) {
        shoppingItems.clear()
        shoppingItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: ShoppingItem) {
        shoppingItems.add(item)
        notifyItemInserted(shoppingItems.size - 1)
    }

    fun removeItem(item: ShoppingItem) {
        val position = shoppingItems.indexOfFirst { it.name == item.name }
        if (position != -1) {
            shoppingItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping, parent, false)
        return ShoppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val item = shoppingItems[position]

        holder.textItemName.text = item.name
        holder.textQuantity.text = item.quantity
        holder.checkBox.isChecked = item.isChecked

        // Cocher/décocher un item
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            onItemCheckedChange?.invoke(item)
        }

        // Supprimer un item
        holder.imageDelete.setOnClickListener {
            onItemDelete?.invoke(item)
        }

        // Barrer le texte si l'item est coché
        if (item.isChecked) {
            holder.textItemName.paintFlags =
                holder.textItemName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.textQuantity.paintFlags =
                holder.textQuantity.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.textItemName.paintFlags =
                holder.textItemName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.textQuantity.paintFlags =
                holder.textQuantity.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount() = shoppingItems.size

    fun getItems() = shoppingItems.toList()
    fun getCheckedItems() = shoppingItems.filter { it.isChecked }
    fun getUncheckedItems() = shoppingItems.filter { !it.isChecked }
}