package com.wellcheck.app

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.wellcheck.app.databinding.ActivityCounselorDashboardBinding
import com.wellcheck.app.network.AppointmentResponse
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CounselorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorDashboardBinding
    private lateinit var adapter: PendingRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "Counselor") ?: "Counselor"

        binding.tvCounselorNameTop.text = firstName
        binding.tvAvatar.text = firstName.firstOrNull()?.uppercase() ?: "C"
        binding.tvGreeting.text = "Welcome, $firstName!"

        setupRecyclerView()
        fetchDashboardData()

        // Button to Manage/Create Slots
        binding.btnCreateSlot.setOnClickListener {
            val intent = Intent(this, ManageSlotsActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PendingRequestAdapter(emptyList()) { appointment ->
            showReviewDialog(appointment)
        }
        binding.rvPendingRequests.layoutManager = LinearLayoutManager(this)
        binding.rvPendingRequests.adapter = adapter
    }

    private fun fetchDashboardData() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselorAppointments(token)
                if (response.isSuccessful) {
                    val all = response.body() ?: emptyList()

                    // Filter using your Enum values
                    val pending = all.filter { it.status.equals("PENDING", ignoreCase = true) }
                    val confirmed = all.filter { it.status.equals("CONFIRMED", ignoreCase = true) }

                    // Update UI Numbers
                    binding.tvPendingCount.text = pending.size.toString()
                    binding.tvConfirmedCount.text = confirmed.size.toString() // This will now show 5
                    binding.tvTotalCount.text = all.size.toString()

                    adapter.updateList(pending)
                }
            } catch (e: Exception) {
                Toast.makeText(this@CounselorDashboardActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showReviewDialog(appointment: AppointmentResponse) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_appointment_details)

        // Make background transparent for rounded corners
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 1. Bind Header Views
        val tvName = dialog.findViewById<TextView>(R.id.tvDialogName)
        val tvStudentId = dialog.findViewById<TextView>(R.id.tvDialogStudentId)
        val tvAvatar = dialog.findViewById<TextView>(R.id.tvDialogAvatar)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnCloseDialog)

        // 2. Bind Detail Grid Views (Ensure these IDs exist in your XML)
        val tvProgram = dialog.findViewById<TextView>(R.id.tvDialogProgram) // Set text to appointment.studentProgram
        val tvYear = dialog.findViewById<TextView>(R.id.tvDialogYear)       // Set text to appointment.studentYearLevel
        val tvGender = dialog.findViewById<TextView>(R.id.tvDialogGender)   // Set text to appointment.studentGender
        val tvBirthdate = dialog.findViewById<TextView>(R.id.tvDialogBirthdate) // Set text to appointment.studentBirthdate

        // 3. Bind Schedule & Action Views
        val tvDate = dialog.findViewById<TextView>(R.id.tvDialogDate)
        val tvTime = dialog.findViewById<TextView>(R.id.tvDialogTime)
        val btnApprove = dialog.findViewById<MaterialButton>(R.id.btnDialogApprove)
        val btnReject = dialog.findViewById<MaterialButton>(R.id.btnDialogReject)

        // --- POPULATE DATA ---

        // Combine First and Last Name
        val fullName = "${appointment.studentFirstName} ${appointment.studentLastName}"
        tvName.text = fullName
        tvStudentId.text = "ID: ${appointment.studentIdNumber}"
        tvAvatar.text = appointment.studentFirstName?.firstOrNull()?.uppercase() ?: "S"

        // Fill the Info Grid
        tvProgram?.text = appointment.studentProgram ?: "N/A"
        tvYear?.text = appointment.studentYearLevel ?: "N/A"
        tvGender?.text = appointment.studentGender ?: "N/A"
        tvBirthdate?.text = appointment.studentBirthdate ?: "N/A"

        // Format Date and Time
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateStart = inputFormat.parse(appointment.startTime)
            val dateEnd = inputFormat.parse(appointment.endTime)

            if (dateStart != null && dateEnd != null) {
                val dateDisplay = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(dateStart)
                val timeDisplay = "${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(dateStart)} → ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(dateEnd)}"

                tvDate.text = "📅 $dateDisplay"
                tvTime.text = "🕒 $timeDisplay"
            }
        } catch (e: Exception) {
            tvDate.text = "Invalid Date"
        }

        // --- BUTTON LOGIC ---

        btnClose.setOnClickListener { dialog.dismiss() }

        btnApprove.setOnClickListener {
            dialog.dismiss()
            // Pass 'true' to trigger the CONFIRMED update
            processDecision(appointment.id, true)
        }

        btnReject.setOnClickListener {
            dialog.dismiss()
            // Pass 'false' to trigger the REJECTED update
            processDecision(appointment.id, false)
        }

        dialog.show()
    }

    private fun processDecision(appointmentId: Long, approve: Boolean) {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = if (approve) {
                    RetrofitClient.instance.approveAppointment(token, appointmentId)
                } else {
                    RetrofitClient.instance.rejectAppointment(token, appointmentId)
                }

                if (response.isSuccessful) {
                    val statusText = if (approve) "Confirmed!" else "Rejected."
                    Toast.makeText(this@CounselorDashboardActivity, statusText, Toast.LENGTH_SHORT).show()
                    // Refresh data so the "5" confirmed count updates immediately
                    fetchDashboardData()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CounselorDashboardActivity, "Connection error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}