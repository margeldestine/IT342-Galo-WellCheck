package com.wellcheck.app

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wellcheck.app.databinding.ActivityCounselorRequestsBinding
import com.wellcheck.app.network.AppointmentResponse
import com.wellcheck.app.network.NetworkConfig
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CounselorRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorRequestsBinding
    private var token: String = ""

    private var allAppointments: List<AppointmentResponse> = emptyList()
    private var currentTab: String = "PENDING"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        // Apply italic DM Serif to title
        val dmSerifItalic = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvTitle.typeface = dmSerifItalic

        setupTabs()
        setupSearch()
        setupBottomNavigation()
        fetchAppointments()
    }

    private fun setupTabs() {
        val tabs = listOf(
            binding.tabPending   to "PENDING",
            binding.tabConfirmed to "CONFIRMED",
            binding.tabRejected  to "REJECTED",
            binding.tabCancelled to "CANCELLED"
        )
        tabs.forEach { (view, status) ->
            view.setOnClickListener {
                currentTab = status
                updateTabUI(status)
                applyFilters()
            }
        }
        updateTabUI("PENDING")
    }

    private fun updateTabUI(activeStatus: String) {
        val tabMap = mapOf(
            "PENDING"   to binding.tabPending,
            "CONFIRMED" to binding.tabConfirmed,
            "REJECTED"  to binding.tabRejected,
            "CANCELLED" to binding.tabCancelled
        )
        val interBold    = ResourcesCompat.getFont(this, R.font.inter_bold)
        val interRegular = ResourcesCompat.getFont(this, R.font.inter_regular)

        tabMap.forEach { (status, view) ->
            if (status == activeStatus) {
                view.setBackgroundResource(R.drawable.tab_active_bg)
                view.setTextColor(Color.WHITE)
                view.typeface = interBold
            } else {
                view.background = null
                view.setTextColor(Color.parseColor("#888888"))
                view.typeface = interRegular
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilters() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val filtered = allAppointments
            .filter { it.status == currentTab }
            .filter {
                if (query.isEmpty()) true
                else "${it.studentFirstName} ${it.studentLastName}".lowercase().contains(query)
            }
            .sortedBy { it.startTime }
        renderList(filtered)
    }

    private fun fetchAppointments() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselorAppointments("Bearer $token")
                if (response.isSuccessful) {
                    allAppointments = response.body() ?: emptyList()
                    runOnUiThread { applyFilters() }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CounselorRequestsActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorRequestsActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread { binding.progressBar.visibility = View.GONE }
            }
        }
    }

    private fun renderList(list: List<AppointmentResponse>) {
        binding.listContainer.removeAllViews()

        val interBold    = ResourcesCompat.getFont(this, R.font.inter_bold)
        val interRegular = ResourcesCompat.getFont(this, R.font.inter_regular)

        val labelMap = mapOf(
            "PENDING"   to "PENDING REQUESTS",
            "CONFIRMED" to "CONFIRMED REQUESTS",
            "REJECTED"  to "REJECTED REQUESTS",
            "CANCELLED" to "CANCELLED REQUESTS"
        )
        binding.tvTabLabel.text     = labelMap[currentTab]
        binding.tvTabLabel.typeface = interBold
        binding.tvTabCount.text     = list.size.toString()
        binding.tvTabCount.typeface = interBold

        if (list.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.tvEmptyMessage.text = "No ${currentTab.lowercase()} requests found."
            return
        }
        binding.layoutEmpty.visibility = View.GONE

        list.forEach { apt ->
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_appointment_card, binding.listContainer, false)

            val fName = apt.studentFirstName ?: ""
            val lName = apt.studentLastName  ?: ""

            // ── Accent bar color per status ──────────────────────────────────
            val accentBar = card.findViewById<View>(R.id.viewAccentBar)
            val accentColor = when (apt.status) {
                "CONFIRMED" -> Color.parseColor("#16A34A") // green
                "REJECTED"  -> Color.parseColor("#DC2626") // red
                "CANCELLED" -> Color.parseColor("#9CA3AF") // gray
                else        -> Color.parseColor("#D97706") // amber — PENDING
            }
            accentBar.setBackgroundColor(accentColor)

            // Avatar
            val tvInitials = card.findViewById<TextView>(R.id.tvInitials)
            tvInitials.text     = "${fName.firstOrNull() ?: ""}${lName.firstOrNull() ?: ""}".uppercase()
            tvInitials.typeface = interBold

            // Name
            val tvName = card.findViewById<TextView>(R.id.tvStudentName)
            tvName.text     = "$fName $lName".trim()
            tvName.typeface = interBold

            // ID
            card.findViewById<TextView>(R.id.tvStudentId).apply {
                text     = apt.studentIdNumber ?: ""
                typeface = interRegular
            }

            // Program • Year
            card.findViewById<TextView>(R.id.tvProgram).apply {
                text     = "${apt.studentProgram ?: ""}  •  ${apt.studentYearLevel ?: ""}"
                typeface = interRegular
            }

            // Date + Time
            val tvDate = card.findViewById<TextView>(R.id.tvDate)
            val tvTime = card.findViewById<TextView>(R.id.tvTime)
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(apt.startTime)
                parsed?.let {
                    tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it)
                    tvTime.text = SimpleDateFormat("h:mm a",      Locale.getDefault()).format(it)
                }
            } catch (e: Exception) {
                tvDate.text = apt.startTime
            }
            tvDate.typeface = interRegular
            tvTime.typeface = interRegular

            // Note
            card.findViewById<TextView>(R.id.tvNote).apply {
                if (!apt.note.isNullOrBlank()) {
                    text       = "Note: ${apt.note}"
                    visibility = View.VISIBLE
                    typeface   = interRegular
                } else {
                    visibility = View.GONE
                }
            }

            // Rejection reason
            card.findViewById<TextView>(R.id.tvRejectionReason).apply {
                if (currentTab == "REJECTED" && !apt.rejectionReason.isNullOrBlank()) {
                    text       = "Reason: ${apt.rejectionReason}"
                    visibility = View.VISIBLE
                    typeface   = interRegular
                } else {
                    visibility = View.GONE
                }
            }

            // Action buttons vs status badge
            val layoutActions = card.findViewById<View>(R.id.layoutActions)
            val tvStatus       = card.findViewById<TextView>(R.id.tvStatus)

            if (currentTab == "PENDING") {
                layoutActions.visibility = View.VISIBLE
                tvStatus.visibility      = View.GONE

                card.findViewById<Button>(R.id.btnApprove).apply {
                    typeface = interBold
                    setOnClickListener { confirmAction(apt.id, "approve") }
                }
                card.findViewById<Button>(R.id.btnReject).apply {
                    typeface = interBold
                    setOnClickListener { showRejectDialog(apt.id) }
                }
            } else {
                layoutActions.visibility = View.GONE
                tvStatus.visibility      = View.VISIBLE
                tvStatus.typeface        = interBold
                tvStatus.text            = apt.status

                when (apt.status) {
                    "CONFIRMED" -> {
                        tvStatus.setTextColor(Color.parseColor("#16A34A"))
                        tvStatus.setBackgroundResource(R.drawable.badge_available_bg)
                    }
                    "REJECTED" -> {
                        tvStatus.setTextColor(Color.parseColor("#DC2626"))
                        tvStatus.setBackgroundResource(R.drawable.bg_badge_rejected)
                    }
                    "CANCELLED" -> {
                        // Gray badge for cancelled
                        tvStatus.setTextColor(Color.parseColor("#6B7280"))
                        tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled)
                    }
                    else -> {
                        tvStatus.setTextColor(Color.parseColor("#D97706"))
                        tvStatus.setBackgroundResource(R.drawable.badge_booked_bg)
                    }
                }
            }

            // Tap card → show school ID photo modal
            card.setOnClickListener {
                val photoUrl = apt.studentSchoolIdPhotoUrl
                if (photoUrl.isNullOrBlank()) {
                    Toast.makeText(this, "No school ID photo uploaded.", Toast.LENGTH_SHORT).show()
                } else {
                    showSchoolIdModal(photoUrl)
                }
            }

            binding.listContainer.addView(card)
        }
    }

    // School ID photo modal
    private fun showSchoolIdModal(photoUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_school_id)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val ivPhoto  = dialog.findViewById<ImageView>(R.id.ivSchoolIdPhoto)
        val btnClose = dialog.findViewById<View>(R.id.btnCloseSchoolId)

        val fullUrl = if (photoUrl.startsWith("http")) photoUrl
        else "${NetworkConfig.BASE_URL}uploads/$photoUrl"

        Glide.with(this).load(fullUrl).into(ivPhoto)
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun confirmAction(id: Long, action: String) {
        val label = if (action == "approve") "Approve" else "Reject"
        AlertDialog.Builder(this)
            .setTitle("$label Appointment")
            .setMessage("Are you sure you want to $label this request?")
            .setPositiveButton(label) { _, _ -> performAction(id, action) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectDialog(id: Long) {
        val input = EditText(this).apply { hint = "Enter rejection reason..." }
        AlertDialog.Builder(this)
            .setTitle("Reject Request")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isNotBlank()) performAction(id, "reject", reason)
                else Toast.makeText(this, "Please enter a reason.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performAction(id: Long, action: String, reason: String? = null) {
        lifecycleScope.launch {
            try {
                val response = if (action == "approve") {
                    RetrofitClient.instance.approveAppointment("Bearer $token", id)
                } else {
                    RetrofitClient.instance.rejectAppointment(
                        "Bearer $token", id, mapOf("reason" to (reason ?: ""))
                    )
                }
                if (response.isSuccessful) {
                    runOnUiThread {
                        val msg = if (action == "approve") "Appointment confirmed!" else "Appointment rejected."
                        Toast.makeText(this@CounselorRequestsActivity, msg, Toast.LENGTH_SHORT).show()
                        fetchAppointments()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CounselorRequestsActivity, "Action failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorRequestsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            startActivity(Intent(this, CounselorDashboardActivity::class.java))
            finish(); overridePendingTransition(0, 0)
        }
        binding.navManageSlots.setOnClickListener {
            startActivity(Intent(this, CounselorManageSlotsActivity::class.java))
            finish(); overridePendingTransition(0, 0)
        }
        binding.navRequests.setOnClickListener { /* already here */ }
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, CounselorProfileActivity::class.java))
            finish(); overridePendingTransition(0, 0)
        }
    }
}