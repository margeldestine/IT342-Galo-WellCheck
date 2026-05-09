package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityCounselorRequestsBinding
import com.wellcheck.app.network.AppointmentResponse
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

        setupTabs()
        setupSearch()
        setupBottomNavigation()
        fetchAppointments()
    }

    private fun setupTabs() {
        val tabs = listOf(
            binding.tabPending to "PENDING",
            binding.tabConfirmed to "CONFIRMED",
            binding.tabRejected to "REJECTED",
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
            "PENDING" to binding.tabPending,
            "CONFIRMED" to binding.tabConfirmed,
            "REJECTED" to binding.tabRejected,
            "CANCELLED" to binding.tabCancelled
        )
        tabMap.forEach { (status, view) ->
            if (status == activeStatus) {
                view.setBackgroundResource(R.drawable.tab_active_bg)
                view.setTextColor(getColor(R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.tab_inactive_bg)
                view.setTextColor(getColor(R.color.text_secondary))
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
                else {
                    val fullName = "${it.studentFirstName} ${it.studentLastName}".lowercase()
                    fullName.contains(query)
                }
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

        // Update tab count label
        binding.tvTabLabel.text = "${currentTab.lowercase().replaceFirstChar { it.uppercase() }} Requests"
        binding.tvTabCount.text = list.size.toString()

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
            val lName = apt.studentLastName ?: ""

            card.findViewById<TextView>(R.id.tvInitials).text =
                "${fName.firstOrNull() ?: ""}${lName.firstOrNull() ?: ""}".uppercase()
            card.findViewById<TextView>(R.id.tvStudentName).text = "$fName $lName"
            card.findViewById<TextView>(R.id.tvStudentId).text = apt.studentIdNumber ?: ""
            card.findViewById<TextView>(R.id.tvProgram).text =
                "${apt.studentProgram ?: ""} • ${apt.studentYearLevel ?: ""}"

            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val parsed = sdf.parse(apt.startTime)
                parsed?.let {
                    card.findViewById<TextView>(R.id.tvDate).text =
                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it)
                    card.findViewById<TextView>(R.id.tvTime).text =
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
                }
            } catch (e: Exception) {
                card.findViewById<TextView>(R.id.tvDate).text = apt.startTime
            }

            // Note
            val tvNote = card.findViewById<TextView>(R.id.tvNote)
            if (!apt.note.isNullOrBlank()) {
                tvNote.text = "Note: ${apt.note}"
                tvNote.visibility = View.VISIBLE
            } else {
                tvNote.visibility = View.GONE
            }

            // Rejection reason (only shown on REJECTED tab)
            val tvRejection = card.findViewById<TextView>(R.id.tvRejectionReason)
            if (currentTab == "REJECTED" && !apt.rejectionReason.isNullOrBlank()) {
                tvRejection.text = "Reason: ${apt.rejectionReason}"
                tvRejection.visibility = View.VISIBLE
            } else {
                tvRejection.visibility = View.GONE
            }

            // Action buttons — only visible on PENDING tab
            val btnActions = card.findViewById<View>(R.id.layoutActions)
            if (currentTab == "PENDING") {
                btnActions.visibility = View.VISIBLE
                card.findViewById<Button>(R.id.btnApprove).setOnClickListener {
                    confirmAction(apt.id, "approve")
                }
                card.findViewById<Button>(R.id.btnReject).setOnClickListener {
                    showRejectDialog(apt.id)
                }
            } else {
                btnActions.visibility = View.GONE
            }

            // Status badge
            val tvStatus = card.findViewById<TextView>(R.id.tvStatus)
            tvStatus.text = apt.status
            tvStatus.setTextColor(
                when (apt.status) {
                    "CONFIRMED" -> getColor(R.color.status_confirmed)
                    "REJECTED"  -> getColor(R.color.status_rejected)
                    "CANCELLED" -> getColor(R.color.status_cancelled)
                    else        -> getColor(R.color.status_pending)
                }
            )

            binding.listContainer.addView(card)
        }
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
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navManageSlots.setOnClickListener {
            startActivity(Intent(this, CounselorManageSlotsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navRequests.setOnClickListener { /* Already here */ }

        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, CounselorProfileActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
    }
}