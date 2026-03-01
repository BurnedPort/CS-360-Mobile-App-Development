package com.example.inventorytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(
    private var items: List<InventoryItem>,
    private val onDelete: (InventoryItem) -> Unit,
    private val onRowClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.VH>() {

    fun update(newItems: List<InventoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val nameText: TextView = v.findViewById(R.id.itemNameText)
        val qtyText: TextView = v.findViewById(R.id.itemQtyText)
        val deleteBtn: Button = v.findViewById(R.id.deleteRowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_inventory, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.nameText.text = item.name
        holder.qtyText.text = item.qty.toString()

        holder.deleteBtn.setOnClickListener { onDelete(item) }
        holder.itemView.setOnClickListener { onRowClick(item) }
    }
}