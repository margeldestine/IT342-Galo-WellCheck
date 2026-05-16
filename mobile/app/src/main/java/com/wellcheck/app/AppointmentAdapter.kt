package com.wellcheck.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellcheck.app.R
import com.wellcheck.app.network.AppointmentResponse
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(
    private val context: Context,
    private var appointments: List<AppointmentResponse>,
    private val onCancelClicked: (AppointmentResponse) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    fun updateData(newData: List<AppointmentResponse>) {
        appointments = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = appointments[position]

        // Date box
        holder.tvMonth.text = formatMonth(apt.startTime)
        holder.tvDay.text   = formatDay(apt.startTime)

        // Counselor info
        holder.tvCounselorName.text  = "${apt.counselorFirstName ?: ""} ${apt.counselorLastName ?: ""}".trim()
        holder.tvSpecialization.text = apt.counselorSpecialization ?: ""
        holder.tvTime.text           = formatTime(apt.startTime)

        // Rejection reason
        if (!apt.rejectionReason.isNullOrEmpty() && apt.status.uppercase() == "REJECTED") {
            holder.layoutReason.visibility = View.VISIBLE
            holder.tvReason.text = "Reason: ${apt.rejectionReason}"
        } else {
            holder.layoutReason.visibility = View.GONE
        }

        // Cancel button — only for PENDING
        if (apt.status.uppercase() == "PENDING") {
            holder.btnCancel.visibility = View.VISIBLE
            holder.btnCancel.setOnClickListener { onCancelClicked(apt) }
        } else {
            holder.btnCancel.visibility = View.GONE
        }

        // Status badge
        val (bgRes, textColor, label) = when (apt.status.uppercase()) {
            "CONFIRMED" -> Triple(R.drawable.bg_badge_confirmed, 0xFF16A34A.toInt(), "Confirmed")
            "PENDING"   -> Triple(R.drawable.bg_badge_pending,   0xFFD97706.toInt(), "Pending")
            "REJECTED"  -> Triple(R.drawable.bg_badge_rejected,  0xFFDC2626.toInt(), "Rejected")
            "CANCELLED" -> Triple(R.drawable.bg_badge_cancelled, 0xFF6B7280.toInt(), "Cancelled")
            else        -> Triple(R.drawable.bg_badge_pending,   0xFFD97706.toInt(), apt.status)
        }
        holder.tvStatus.setBackgroundResource(bgRes)
        holder.tvStatus.setTextColor(textColor)
        holder.tvStatus.text = label

        // Date box color — all use green like the web
        val dateBoxRes = when (apt.status.uppercase()) {
            "REJECTED"  -> R.drawable.bg_date_box_red
            "CANCELLED" -> R.drawable.bg_date_box_gray
            else        -> R.drawable.bg_date_box_green  // PENDING + CONFIRMED both green
        }
        holder.layoutDateBox.setBackgroundResource(dateBoxRes)

        val monthColor = when (apt.status.uppercase()) {
            "REJECTED"  -> 0xFFDC2626.toInt()
            "CANCELLED" -> 0xFF6B7280.toInt()
            else        -> 0xFF16A34A.toInt()  // green for PENDING + CONFIRMED
        }
        holder.tvMonth.setTextColor(monthColor)
    }

    override fun getItemCount() = appointments.size

    private fun formatMonth(dt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val d   = sdf.parse(dt)
            SimpleDateFormat("MMM", Locale.ENGLISH).format(d ?: return "").uppercase()
        } catch (e: Exception) { "" }
    }

    private fun formatDay(dt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val d   = sdf.parse(dt)
            SimpleDateFormat("d", Locale.ENGLISH).format(d ?: return "")
        } catch (e: Exception) { "" }
    }

    private fun formatTime(dt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val d   = sdf.parse(dt)
            SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(d ?: return dt)
        } catch (e: Exception) { dt }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutDateBox: LinearLayout = view.findViewById(R.id.layoutDateBox)
        val tvMonth: TextView           = view.findViewById(R.id.tvMonth)
        val tvDay: TextView             = view.findViewById(R.id.tvDay)
        val tvCounselorName: TextView   = view.findViewById(R.id.tvCounselorName)
        val tvSpecialization: TextView  = view.findViewById(R.id.tvSpecialization)
        val tvTime: TextView            = view.findViewById(R.id.tvTime)
        val tvStatus: TextView          = view.findViewById(R.id.tvStatus)
        val btnCancel: TextView          = view.findViewById(R.id.btnCancel)
        val layoutReason: LinearLayout  = view.findViewById(R.id.layoutReason)
        val tvReason: TextView          = view.findViewById(R.id.tvReason)
    }
}