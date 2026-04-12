package com.wellcheck.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wellcheck.app.databinding.ItemSlotBinding
import com.wellcheck.app.network.SlotResponse
import java.text.SimpleDateFormat
import java.util.*

class SlotAdapter(
    private val slots: List<SlotResponse>,
    private val onDeleteClick: (Long) -> Unit
) : RecyclerView.Adapter<SlotAdapter.SlotViewHolder>() {

    class SlotViewHolder(val binding: ItemSlotBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val binding = ItemSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        try {
            val start = inputFormat.parse(slot.startTime)
            val end = inputFormat.parse(slot.endTime)
            if (start != null && end != null) {
                holder.binding.tvSlotTime.text = "${timeFormat.format(start)} → ${timeFormat.format(end)}"
                holder.binding.tvSlotDate.text = dateFormat.format(start)
                holder.binding.tvDay.text = dayFormat.format(start)
                holder.binding.tvMonth.text = monthFormat.format(start).uppercase()
            }
        } catch (e: Exception) {
            holder.binding.tvSlotTime.text = "Error"
        }

        val statusStr = slot.status?.uppercase() ?: "AVAILABLE"
        holder.binding.tvStatus.text = statusStr

        when (statusStr) {
            "BOOKED" -> {
                holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#C05621"))
                holder.binding.btnRemove.visibility = android.view.View.GONE
            }
            else -> {
                holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#2D6A4F"))
                holder.binding.btnRemove.visibility = android.view.View.VISIBLE
            }
        }

        holder.binding.btnRemove.setOnClickListener { onDeleteClick(slot.id) }
    }

    override fun getItemCount() = slots.size
}