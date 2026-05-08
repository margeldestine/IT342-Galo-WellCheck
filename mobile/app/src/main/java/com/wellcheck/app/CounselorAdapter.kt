package com.wellcheck.app.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.wellcheck.app.R
import com.wellcheck.app.network.CounselorListItem
import de.hdodenhof.circleimageview.CircleImageView

class CounselorAdapter(
    private val context: Context,
    private var counselors: List<CounselorListItem>,
    private val onSlotsClicked: (CounselorListItem) -> Unit
) : RecyclerView.Adapter<CounselorAdapter.ViewHolder>() {

    fun updateData(newData: List<CounselorListItem>) {
        counselors = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_browse_counselor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val counselor = counselors[position]

        holder.tvName.text = "${counselor.firstName} ${counselor.lastName}"
        holder.tvSpecialization.text = counselor.specialization
        holder.tvBio.text = counselor.bio ?: "No bio available."

        // Handle Profile Photo
        if (!counselor.profilePhoto.isNullOrEmpty()) {
            holder.ivAvatar.visibility = View.VISIBLE
            holder.tvInitials.visibility = View.GONE
            Glide.with(context).load(counselor.profilePhoto).circleCrop().into(holder.ivAvatar)
        } else {
            holder.ivAvatar.visibility = View.GONE
            holder.tvInitials.visibility = View.VISIBLE
            holder.tvInitials.text = "${counselor.firstName?.firstOrNull() ?: ""}${counselor.lastName?.firstOrNull() ?: ""}".uppercase()
        }

        // Handle Availability & Status
        if (counselor.availableSlots > 0) {
            holder.vStatusDot.backgroundTintList = context.getColorStateList(android.R.color.holo_green_dark)
            holder.tvStatusText.text = "Available"
            holder.tvStatusText.setTextColor(Color.parseColor("#10B981"))

            holder.tvSlotsCount.text = "${counselor.availableSlots} slot${if (counselor.availableSlots != 1) "s" else ""} available"
            holder.btnViewSlots.text = "View available slots"
            holder.btnViewSlots.isEnabled = true
            holder.btnViewSlots.setTextColor(context.getColor(R.color.green_dark))
            holder.btnViewSlots.strokeColor = context.getColorStateList(R.color.green_dark)
        } else {
            holder.vStatusDot.backgroundTintList = context.getColorStateList(android.R.color.darker_gray)
            holder.tvStatusText.text = "Unavailable"
            holder.tvStatusText.setTextColor(context.getColor(R.color.text_gray))

            holder.tvSlotsCount.text = "No slots available"
            holder.btnViewSlots.text = "No slots available"
            holder.btnViewSlots.isEnabled = false
            holder.btnViewSlots.setTextColor(context.getColor(R.color.text_gray))
            holder.btnViewSlots.strokeColor = context.getColorStateList(R.color.border)
        }

        holder.btnViewSlots.setOnClickListener {
            onSlotsClicked(counselor)
        }
    }

    override fun getItemCount() = counselors.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: CircleImageView = view.findViewById(R.id.ivAvatar)
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSpecialization: TextView = view.findViewById(R.id.tvSpecialization)
        val vStatusDot: View = view.findViewById(R.id.vStatusDot)
        val tvStatusText: TextView = view.findViewById(R.id.tvStatusText)
        val tvBio: TextView = view.findViewById(R.id.tvBio)
        val tvSlotsCount: TextView = view.findViewById(R.id.tvSlotsCount)
        val btnViewSlots: MaterialButton = view.findViewById(R.id.btnViewSlots)
    }
}