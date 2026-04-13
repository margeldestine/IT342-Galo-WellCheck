package com.wellcheck.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wellcheck.app.databinding.ItemCounselorBinding
import com.wellcheck.app.network.CounselorResponse

class CounselorAdapter(
    private var counselors: List<CounselorResponse>,
    private val onViewSlotsClick: (CounselorResponse) -> Unit
) : RecyclerView.Adapter<CounselorAdapter.CounselorViewHolder>() {

    class CounselorViewHolder(val binding: ItemCounselorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounselorViewHolder {
        val binding = ItemCounselorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CounselorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CounselorViewHolder, position: Int) {
        val counselor = counselors[position]

        holder.binding.tvCounselorName.text = "${counselor.firstName} ${counselor.lastName}"
        holder.binding.tvSpecialization.text = counselor.specialization
        holder.binding.tvDescription.text = counselor.description

        val initials = "${counselor.firstName.firstOrNull() ?: ""}${counselor.lastName.firstOrNull() ?: ""}"
        holder.binding.tvAvatar.text = initials.uppercase()

        val slotsText = if (counselor.availableSlotsCount == 1) "1 available slot" else "${counselor.availableSlotsCount} available slots"
        holder.binding.tvSlotsCount.text = "📅 $slotsText"

        if (counselor.availableSlotsCount > 0) {
            holder.binding.btnViewSlots.isEnabled = true
            holder.binding.btnViewSlots.text = "View available slots"
            holder.binding.btnViewSlots.setBackgroundColor(android.graphics.Color.parseColor("#2D6A4F"))
        } else {
            holder.binding.btnViewSlots.isEnabled = false
            holder.binding.btnViewSlots.text = "No slots available"
            holder.binding.btnViewSlots.setBackgroundColor(android.graphics.Color.parseColor("#EAEAEA"))
            holder.binding.btnViewSlots.setTextColor(android.graphics.Color.GRAY)
        }

        holder.binding.btnViewSlots.setOnClickListener {
            onViewSlotsClick(counselor)
        }
    }

    override fun getItemCount() = counselors.size

    fun updateList(newList: List<CounselorResponse>) {
        counselors = newList
        notifyDataSetChanged()
    }
}