package com.wellcheck.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.wellcheck.app.R
import com.wellcheck.app.network.SlotResponse
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SlotPickerAdapter(
    private val context: Context,
    private var slots: List<SlotResponse>,
    private val onSelectClicked: (SlotResponse) -> Unit
) : RecyclerView.Adapter<SlotPickerAdapter.ViewHolder>() {

    fun updateData(newSlots: List<SlotResponse>) {
        slots = newSlots
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        try {
            val start = ZonedDateTime.parse(slot.startTime)
            val end   = ZonedDateTime.parse(slot.endTime)

            holder.tvSlotMonth.text = start.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)).uppercase()
            holder.tvSlotDay.text   = start.format(DateTimeFormatter.ofPattern("d"))

            val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            holder.tvSlotTime.text = "${start.format(timeFmt)} → ${end.format(timeFmt)}"
            holder.tvSlotDate.text = start.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.ENGLISH))
        } catch (e: Exception) {
            // fallback for non-zoned datetime strings
            try {
                val fmt   = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val start = fmt.parse(slot.startTime)
                val end   = fmt.parse(slot.endTime)
                val monthFmt = java.text.SimpleDateFormat("MMM", Locale.ENGLISH)
                val dayFmt   = java.text.SimpleDateFormat("d", Locale.ENGLISH)
                val timeFmt  = java.text.SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                val dateFmt  = java.text.SimpleDateFormat("EEEE, MMM d, yyyy", Locale.ENGLISH)
                holder.tvSlotMonth.text = start?.let { monthFmt.format(it).uppercase() } ?: ""
                holder.tvSlotDay.text   = start?.let { dayFmt.format(it) } ?: ""
                holder.tvSlotTime.text  = "${start?.let { timeFmt.format(it) }} → ${end?.let { timeFmt.format(it) }}"
                holder.tvSlotDate.text  = start?.let { dateFmt.format(it) } ?: ""
            } catch (e2: Exception) {
                holder.tvSlotMonth.text = ""
                holder.tvSlotDay.text   = ""
                holder.tvSlotTime.text  = slot.startTime
                holder.tvSlotDate.text  = ""
            }
        }

        holder.btnSelectSlot.setOnClickListener { onSelectClicked(slot) }
    }

    override fun getItemCount() = slots.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSlotMonth: TextView         = view.findViewById(R.id.tvSlotMonth)
        val tvSlotDay: TextView           = view.findViewById(R.id.tvSlotDay)
        val tvSlotTime: TextView          = view.findViewById(R.id.tvSlotTime)
        val tvSlotDate: TextView          = view.findViewById(R.id.tvSlotDate)
        val btnSelectSlot: MaterialButton = view.findViewById(R.id.btnSelectSlot)
    }
}