package com.wellcheck.app.adapter

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wellcheck.app.R
import com.wellcheck.app.network.AppointmentResponse
import com.wellcheck.app.network.SlotResponse
import java.text.SimpleDateFormat
import java.util.*

class SlotAdapter(
    private val context: Context,
    private var slots: List<SlotResponse>,
    private val onEdit: (SlotResponse) -> Unit,
    private val onDelete: (Long) -> Unit
) : RecyclerView.Adapter<SlotAdapter.ViewHolder>() {

    // Cross-reference list — set from the Activity after fetching appointments
    var appointments: List<AppointmentResponse> = emptyList()

    // Tracks which slot id is currently expanded
    private var expandedSlotId: Long? = null

    fun updateData(newSlots: List<SlotResponse>) {
        slots = newSlots
        notifyDataSetChanged()
    }

    fun updateAppointments(newAppointments: List<AppointmentResponse>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_slot_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        val interBold    = ResourcesCompat.getFont(context, R.font.inter_bold)
        val interRegular = ResourcesCompat.getFont(context, R.font.inter_regular)
        val dmSerif      = ResourcesCompat.getFont(context, R.font.dm_serif_display_regular)

        // ── Parse dates ──────────────────────────────────────────────────────
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val start  = parser.parse(slot.startTime)
        val end    = parser.parse(slot.endTime)

        if (start != null && end != null) {
            holder.tvDayNum.text  = SimpleDateFormat("dd",   Locale.getDefault()).format(start)
            holder.tvMonth.text   = SimpleDateFormat("MMM",  Locale.getDefault()).format(start).uppercase()
            holder.tvTimeRange.text = buildString {
                append(SimpleDateFormat("h:mm a", Locale.getDefault()).format(start))
                append(" → ")
                append(SimpleDateFormat("h:mm a", Locale.getDefault()).format(end))
            }
            holder.tvFullDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(start)
        }

        holder.tvDayNum.typeface   = interBold
        holder.tvMonth.typeface    = interRegular
        holder.tvTimeRange.typeface = interBold
        holder.tvFullDate.typeface  = interRegular

        // ── Status badge + visibility of actions ────────────────────────────
        holder.tvStatus.typeface = interBold
        if (slot.status == "BOOKED") {
            holder.tvStatus.text = "BOOKED"
            holder.tvStatus.setTextColor(Color.parseColor("#D97706"))
            holder.tvStatus.setBackgroundResource(R.drawable.badge_booked_bg)
            holder.btnEdit.visibility    = View.GONE
            holder.btnDelete.visibility  = View.GONE
            holder.ivChevron.visibility  = View.VISIBLE
        } else {
            holder.tvStatus.text = "AVAILABLE"
            holder.tvStatus.setTextColor(Color.parseColor("#16A34A"))
            holder.tvStatus.setBackgroundResource(R.drawable.badge_available_bg)
            holder.btnEdit.visibility    = View.VISIBLE
            holder.btnDelete.visibility  = View.VISIBLE
            holder.ivChevron.visibility  = View.GONE
        }

        // ── Expand / collapse ────────────────────────────────────────────────
        val isExpanded = expandedSlotId == slot.id
        holder.layoutExpanded.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivChevron.rotation = if (isExpanded) 180f else 0f

        holder.layoutHeader.setOnClickListener {
            if (slot.status != "BOOKED") return@setOnClickListener

            val wasExpanded = expandedSlotId == slot.id
            expandedSlotId = if (wasExpanded) null else slot.id
            notifyDataSetChanged()
        }

        // ── Populate booked student details ─────────────────────────────────
        if (slot.status == "BOOKED") {
            val apt = appointments.find { it.slotId == slot.id }
            if (apt != null) {
                // Initials avatar
                val initials = buildString {
                    apt.studentFirstName?.firstOrNull()?.let { append(it) }
                    apt.studentLastName?.firstOrNull()?.let { append(it) }
                }
                holder.tvAvatar.text     = initials
                holder.tvAvatar.typeface = interBold

                holder.tvStudentName.text     = "${apt.studentFirstName ?: ""} ${apt.studentLastName ?: ""}".trim()
                holder.tvStudentName.typeface  = dmSerif
                holder.tvStudentIdNum.text    = "ID: ${apt.studentIdNumber ?: "—"}"
                holder.tvStudentIdNum.typeface = interRegular
                holder.tvStudentLabel.typeface = interBold

                // Meta chips  — "LABEL value" style matching the web
                holder.tvChipProgram.text  = "PROGRAM  ${apt.studentProgram ?: "—"}"
                holder.tvChipYear.text     = "YEAR  ${apt.studentYearLevel ?: "—"}"
                holder.tvChipGender.text   = "GENDER  ${apt.studentGender ?: "—"}"

                val dobText = apt.studentBirthdate?.let {
                    try {
                        val dobParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dobDate   = dobParser.parse(it)
                        if (dobDate != null)
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(dobDate)
                        else it
                    } catch (e: Exception) { it }
                } ?: "—"
                holder.tvChipDob.text = "DOB  $dobText"

                listOf(holder.tvChipProgram, holder.tvChipYear, holder.tvChipGender, holder.tvChipDob)
                    .forEach { it.typeface = interRegular }

                // Note
                if (!apt.note.isNullOrBlank()) {
                    holder.tvNote.text       = "\"${apt.note}\""
                    holder.tvNote.visibility = View.VISIBLE
                    holder.tvNote.typeface   = interRegular
                } else {
                    holder.tvNote.visibility = View.GONE
                }

                // View School ID
                holder.btnViewSchoolId.setOnClickListener {
                    if (apt.studentSchoolIdPhotoUrl.isNullOrBlank()) {
                        Toast.makeText(context, "No school ID photo uploaded.", Toast.LENGTH_SHORT).show()
                    } else {
                        showSchoolIdModal(apt.studentSchoolIdPhotoUrl)
                    }
                }
            }
        }

        // ── Standard button listeners ────────────────────────────────────────
        holder.btnEdit.setOnClickListener   { onEdit(slot) }
        holder.btnDelete.setOnClickListener { onDelete(slot.id) }
    }

    override fun getItemCount() = slots.size

    // ── School ID photo modal ────────────────────────────────────────────────
    private fun showSchoolIdModal(photoUrl: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_school_id)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val ivPhoto   = dialog.findViewById<ImageView>(R.id.ivSchoolIdPhoto)
        val btnClose  = dialog.findViewById<View>(R.id.btnCloseSchoolId)

        val fullUrl = if (photoUrl.startsWith("http")) photoUrl
        else "${com.wellcheck.app.network.NetworkConfig.BASE_URL}uploads/$photoUrl"

        Glide.with(context).load(fullUrl).into(ivPhoto)
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutHeader: LinearLayout   = view.findViewById(R.id.layoutSlotHeader)
        val tvDayNum: TextView           = view.findViewById(R.id.tvSlotDayNum)
        val tvMonth: TextView            = view.findViewById(R.id.tvSlotMonth)
        val tvTimeRange: TextView        = view.findViewById(R.id.tvSlotTime)
        val tvFullDate: TextView         = view.findViewById(R.id.tvSlotDate)
        val tvStatus: TextView           = view.findViewById(R.id.tvSlotStatus)
        val btnEdit: View                = view.findViewById(R.id.btnEditSlot)
        val btnDelete: View              = view.findViewById(R.id.btnDeleteSlot)
        val ivChevron: ImageView         = view.findViewById(R.id.ivChevron)
        val layoutExpanded: LinearLayout = view.findViewById(R.id.layoutBookedExpanded)

        // Expanded section views
        val tvAvatar: TextView        = view.findViewById(R.id.tvStudentAvatar)
        val tvStudentLabel: TextView  = view.findViewById(R.id.tvStudentLabel)
        val tvStudentName: TextView   = view.findViewById(R.id.tvStudentName)
        val tvStudentIdNum: TextView  = view.findViewById(R.id.tvStudentIdNumber)
        val tvChipProgram: TextView   = view.findViewById(R.id.tvChipProgram)
        val tvChipYear: TextView      = view.findViewById(R.id.tvChipYear)
        val tvChipGender: TextView    = view.findViewById(R.id.tvChipGender)
        val tvChipDob: TextView       = view.findViewById(R.id.tvChipDob)
        val tvNote: TextView          = view.findViewById(R.id.tvStudentNote)
        val btnViewSchoolId: View     = view.findViewById(R.id.btnViewSchoolId)
    }
}