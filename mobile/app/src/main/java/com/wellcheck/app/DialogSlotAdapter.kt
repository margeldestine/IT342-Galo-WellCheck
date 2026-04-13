package com.wellcheck.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wellcheck.app.databinding.ItemDialogSlotBinding
import com.wellcheck.app.network.SlotResponse
import java.text.SimpleDateFormat
import java.util.*

class DialogSlotAdapter(
    private val slots: List<SlotResponse>,
    private val onSelectClick: (Long, String) -> Unit // Passes ID and formatted Date/Time String
) : RecyclerView.Adapter<DialogSlotAdapter.SlotViewHolder>() {

    class SlotViewHolder(val binding: ItemDialogSlotBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val binding = ItemDialogSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())

        var formattedDateTimeForNextScreen = "Unknown Time"

        try {
            val start = inputFormat.parse(slot.startTime)
            val end = inputFormat.parse(slot.endTime)

            if (start != null && end != null) {
                val timeRange = "${timeFormat.format(start)} → ${timeFormat.format(end)}"
                val fullDate = fullDateFormat.format(start)

                holder.binding.tvTimeRange.text = timeRange
                holder.binding.tvFullDate.text = fullDate
                holder.binding.tvBadgeMonth.text = monthFormat.format(start).uppercase()
                holder.binding.tvBadgeDay.text = dayFormat.format(start)

                formattedDateTimeForNextScreen = "$fullDate at ${timeFormat.format(start)}"
            }
        } catch (e: Exception) {
            holder.binding.tvTimeRange.text = "Invalid Time"
        }

        holder.binding.btnSelectSlot.setOnClickListener {
            onSelectClick(slot.id, formattedDateTimeForNextScreen)
        }
    }

    override fun getItemCount() = slots.size
}