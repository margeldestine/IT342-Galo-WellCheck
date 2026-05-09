package com.wellcheck.app.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.wellcheck.app.R
import com.wellcheck.app.network.SlotResponse
import java.text.SimpleDateFormat
import java.util.*

class SlotAdapter(
    private val context: Context,
    private var slots: List<SlotResponse>,
    private val onEdit: (SlotResponse) -> Unit,
    private val onDelete: (Long) -> Unit
) : RecyclerView.Adapter<SlotAdapter.ViewHolder>() {

    fun updateData(newData: List<SlotResponse>) {
        slots = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_slot_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        val interBold = ResourcesCompat.getFont(context, R.font.inter_bold)
        val interRegular = ResourcesCompat.getFont(context, R.font.inter_regular)

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val start = parser.parse(slot.startTime)
        val end = parser.parse(slot.endTime)

        if (start != null && end != null) {
            holder.tvDayNum.text = SimpleDateFormat("dd", Locale.getDefault()).format(start)
            holder.tvDayNum.typeface = interBold

            holder.tvMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(start).uppercase()
            holder.tvMonth.typeface = interRegular

            holder.tvTimeRange.text = "${SimpleDateFormat("h:mm a", Locale.getDefault()).format(start)} → ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(end)}"
            holder.tvTimeRange.typeface = interBold

            holder.tvFullDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(start)
            holder.tvFullDate.typeface = interRegular
        }

        // Status badge
        holder.tvStatus.typeface = interBold
        if (slot.status == "BOOKED") {
            holder.tvStatus.text = "BOOKED"
            holder.tvStatus.setTextColor(Color.parseColor("#D97706"))
            holder.tvStatus.setBackgroundResource(R.drawable.badge_booked_bg)
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.tvStatus.text = "AVAILABLE"
            holder.tvStatus.setTextColor(Color.parseColor("#16A34A"))
            holder.tvStatus.setBackgroundResource(R.drawable.badge_available_bg)
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        }

        holder.btnEdit.setOnClickListener { onEdit(slot) }
        holder.btnDelete.setOnClickListener { onDelete(slot.id) }
    }

    override fun getItemCount() = slots.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNum: TextView = view.findViewById(R.id.tvSlotDayNum)
        val tvMonth: TextView = view.findViewById(R.id.tvSlotMonth)
        val tvTimeRange: TextView = view.findViewById(R.id.tvSlotTime)
        val tvFullDate: TextView = view.findViewById(R.id.tvSlotDate)
        val tvStatus: TextView = view.findViewById(R.id.tvSlotStatus)
        val btnEdit: View = view.findViewById(R.id.btnEditSlot)
        val btnDelete: View = view.findViewById(R.id.btnDeleteSlot)
    }
}