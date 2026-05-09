package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityCounselorDashboardBinding
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CounselorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorDashboardBinding
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "Counselor") ?: "Counselor"
        token = prefs.getString("token", "") ?: ""

        setupHeader(firstName)
        setupActions()
        setupBottomNavigation()

        // Pass only the string value, removing the "tokenHeader:" label that caused the error in image_19f27a.jpg
        fetchDashboardData("Bearer $token")
    }

    private fun setupHeader(firstName: String) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        binding.tvWelcome.text = "$greeting, $firstName."

        val cal = Calendar.getInstance()
        binding.tvDateDay.text = cal.get(Calendar.DAY_OF_MONTH).toString()
        binding.tvDateMonth.text = SimpleDateFormat("MMM yyyy", Locale.US).format(cal.time).uppercase()
    }

    private fun setupActions() {
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.cardCreateSlot.setOnClickListener {
            navigateToManageSlots()
        }

        binding.cardReviewRequests.setOnClickListener {
            startActivity(Intent(this, CounselorRequestsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener { /* Already here */ }

        binding.navManageSlots.setOnClickListener {
            navigateToManageSlots()
        }

        binding.navRequests.setOnClickListener {
            startActivity(Intent(this, CounselorRequestsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, CounselorProfileActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun navigateToManageSlots() {
        val intent = Intent(this, CounselorManageSlotsActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun fetchDashboardData(authHeader: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselorAppointments(authHeader)
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()

                    // Filter logic matching the web UI
                    val pendingList = appointments.filter { it.status == "PENDING" }.sortedBy { it.startTime }
                    val confirmedCount = appointments.count { it.status == "CONFIRMED" }
                    val totalStudents = appointments.distinctBy { it.studentFirstName + it.studentLastName }.size

                    runOnUiThread {
                        binding.tvPendingCount.text = pendingList.size.toString()
                        binding.tvConfirmedCount.text = confirmedCount.toString()
                        binding.tvTotalStudentsCount.text = totalStudents.toString()
                        binding.tvPendingSub.text = "${pendingList.size} pending appointment${if (pendingList.size != 1) "s" else ""}"

                        if (pendingList.isEmpty()) {
                            binding.layoutEmptyPending.visibility = View.VISIBLE
                            binding.layoutPendingList.visibility = View.GONE
                        } else {
                            binding.layoutEmptyPending.visibility = View.GONE
                            binding.layoutPendingList.visibility = View.VISIBLE
                            renderPendingRequests(pendingList.take(3))
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@CounselorDashboardActivity, "Connection error", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun renderPendingRequests(requests: List<com.wellcheck.app.network.AppointmentResponse>) {
        binding.layoutPendingList.removeAllViews()
        requests.forEach { apt ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_pending_request_counselor, binding.layoutPendingList, false)

            val fName = apt.studentFirstName ?: ""
            val lName = apt.studentLastName ?: ""
            row.findViewById<TextView>(R.id.tvStudentInitials).text = "${fName.firstOrNull() ?: ""}${lName.firstOrNull() ?: ""}".uppercase()
            row.findViewById<TextView>(R.id.tvStudentName).text = "$fName $lName"

            try {
                val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(apt.startTime)
                parsedDate?.let {
                    row.findViewById<TextView>(R.id.tvDate).text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it)
                    row.findViewById<TextView>(R.id.tvTime).text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
                }
            } catch (e: Exception) {
                row.findViewById<TextView>(R.id.tvDate).text = apt.startTime
            }

            row.findViewById<View>(R.id.btnAccept).setOnClickListener {
                handleAppointmentAction(apt.id, "approve")
            }

            row.findViewById<View>(R.id.btnReject).setOnClickListener {
                showRejectReasonDialog(apt.id)
            }

            binding.layoutPendingList.addView(row)
        }
    }

    private fun handleAppointmentAction(id: Long, action: String, reason: String? = null) {
        lifecycleScope.launch {
            try {
                // Retrofit call needs to be in a coroutine (launch)
                val response = if (action == "approve") {
                    RetrofitClient.instance.approveAppointment("Bearer $token", id)
                } else {
                    // Passes the reason map just like your React code: { reason: "..." }
                    RetrofitClient.instance.rejectAppointment("Bearer $token", id, mapOf("reason" to (reason ?: "")))
                }

                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CounselorDashboardActivity, "Appointment $action successful", Toast.LENGTH_SHORT).show()
                        // Refresh the dashboard data to update the counts and list
                        fetchDashboardData("Bearer $token")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CounselorDashboardActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorDashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRejectReasonDialog(id: Long) {
        val input = EditText(this)
        input.hint = "Enter reason..."
        AlertDialog.Builder(this)
            .setTitle("Reject Request")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val reason = input.text.toString()
                if (reason.isNotBlank()) handleAppointmentAction(id, "reject", reason)
                else Toast.makeText(this, "Reason required", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log out") { _, _ ->
                getSharedPreferences("wellcheck_prefs", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}