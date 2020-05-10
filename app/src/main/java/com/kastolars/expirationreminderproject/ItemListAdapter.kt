package com.kastolars.expirationreminderproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class ItemListAdapter(private val items: ArrayList<Item>) :
    RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    private val tag = "exprem" + ItemListAdapter::class.java.simpleName

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.item_name)
        val dateView: TextView = view.findViewById(R.id.item_expiration_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.v(tag, "getItemCount called")
        Log.d(tag, "Item count: ${items.size}")
        return items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        val name = item.name
        val date = item.expirationDate
        val formatter: SimpleDateFormat = SimpleDateFormat("MM-dd-yyyy")

        holder.nameView.text = name
        holder.dateView.text = formatter.format(date)
    }
}