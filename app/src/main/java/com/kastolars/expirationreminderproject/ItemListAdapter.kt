package com.kastolars.expirationreminderproject

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kastolars.expirationreminderproject.models.Item
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
        return items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        val name = item.name
        val date = item.expirationDate
        val formatter = SimpleDateFormat("M-dd-yyyy", Locale.US)

        val today = Calendar.getInstance(TimeZone.getDefault()).time
        val millisBetween = date.time - today.time
        val daysBetween = TimeUnit.DAYS.convert(millisBetween, TimeUnit.MILLISECONDS)
        val color = when {
            daysBetween > 1 -> {
                Color.parseColor("#00B345")
            }
            daysBetween > 0 -> {
                Color.parseColor("#FFC107")
            }
            else -> {
                Color.parseColor("#FF5722")
            }
        }

        holder.nameView.text = name
        holder.dateView.text = formatter.format(date)
        holder.dateView.setTextColor(color)
    }
}