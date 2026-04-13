package com.wellcheck.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wellcheck.app.databinding.ItemPendingRequestBinding
import com.wellcheck.app.network.AppointmentResponse
import java.text.SimpleDateFormat
import java.util.*

class PendingRequestAdapter(
    private var requests: List<AppointmentResponse>,
    private val onReviewClick: (AppointmentResponse) -> Unit
) : RecyclerView.Adapter<PendingRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(val binding: ItemPendingRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemPendingRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]

        val firstName = request.studentFirstName ?: ""
        val lastName = request.studentLastName ?: ""
        holder.binding.tvStudentName.text = "$firstName $lastName".trim()

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())

        try {
            val start = inputFormat.parse(request.startTime)
            val end = inputFormat.parse(request.endTime)
            if (start != null && end != null) {
                holder.binding.tvTime.text = "${timeFormat.format(start)} → ${timeFormat.format(end)}"
                holder.binding.tvBadgeMonth.text = monthFormat.format(start).uppercase()
                holder.binding.tvBadgeDay.text = dayFormat.format(start)
            }
        } catch (e: Exception) {
            holder.binding.tvTime.text = "Invalid Time"
        }

        // Force all interactions to open the review dialog first
        val clickListener = { _: android.view.View -> onReviewClick(request) }
        holder.binding.root.setOnClickListener(clickListener)
        holder.binding.btnApproveList.setOnClickListener(clickListener)
        holder.binding.btnRejectList.setOnClickListener(clickListener)
    }

    override fun getItemCount() = requests.size

    fun updateList(newList: List<AppointmentResponse>) {
        requests = newList
        notifyDataSetChanged()
    }
}