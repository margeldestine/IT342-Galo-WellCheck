package com.wellcheck.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityBookAppointmentBinding
import com.wellcheck.app.network.AppointmentRequest
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAppointmentBinding
    private var slotId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Get data passed from the Browse screen
        slotId = intent.getLongExtra("SLOT_ID", 0)
        val counselorName = intent.getStringExtra("COUNSELOR_NAME") ?: ""
        val dateTimeStr = intent.getStringExtra("DATE_TIME_STR") ?: ""

        // 2. Setup Header
        binding.tvHeaderSubtitle.text = "With $counselorName on $dateTimeStr"

        // 3. Populate Personal Information from Login
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "Jhoe")
        val lastName = prefs.getString("lastName", "Doe")
        val studentId = prefs.getString("employeeNumber", "2011-89765")

        binding.tvFieldStudentId.text = studentId
        binding.tvFieldFirstName.text = firstName
        binding.tvFieldLastName.text = lastName

        // Prepare Review Text for Step 3
        binding.tvReviewDetails.text = """
            Counselor: $counselorName
            Date/Time: $dateTimeStr
            Student: $firstName $lastName
            Student ID: $studentId
        """.trimIndent()

        // 4. Button Click Listeners
        binding.tvBackToCounselors.setOnClickListener { finish() }

        // Step 1 Buttons
        binding.btnChangeDetails.setOnClickListener {
            Toast.makeText(this, "Profile editing is not available yet.", Toast.LENGTH_SHORT).show()
        }
        binding.btnConfirmDetails.setOnClickListener { showStep(2) }

        // Step 2 Buttons
        binding.btnToStep3.setOnClickListener { showStep(3) }
        binding.btnBackToStep1.setOnClickListener { showStep(1) }

        // Step 3 Buttons
        binding.btnConfirmBooking.setOnClickListener { submitBooking() }
        binding.btnBackToStep2.setOnClickListener { showStep(2) }

        // Success Screen Buttons
        binding.btnViewAppointments.setOnClickListener {
            Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnBackToDashboard.setOnClickListener {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun showStep(step: Int) {
        // Hide all
        binding.layoutStep1.visibility = View.GONE
        binding.layoutStep2.visibility = View.GONE
        binding.layoutStep3.visibility = View.GONE
        binding.layoutSuccess.visibility = View.GONE

        // Show target
        when (step) {
            1 -> binding.layoutStep1.visibility = View.VISIBLE
            2 -> binding.layoutStep2.visibility = View.VISIBLE
            3 -> binding.layoutStep3.visibility = View.VISIBLE
            4 -> binding.layoutSuccess.visibility = View.VISIBLE
        }

        // Hide Stepper and Back text if Success screen
        if (step == 4) {
            binding.layoutStepper.visibility = View.GONE
            binding.tvBackToCounselors.visibility = View.INVISIBLE
            return
        }

        // Update Stepper Visuals (1, 2, 3)
        val activeColor = Color.parseColor("#2D6A4F")
        val inactiveColor = Color.parseColor("#DDDDDD")
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#888888")

        // Step 2 logic
        if (step >= 2) {
            binding.step2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor))
            binding.step2.setTextColor(activeText)
            binding.line1.setBackgroundColor(activeColor)
        } else {
            binding.step2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor))
            binding.step2.setTextColor(inactiveText)
            binding.line1.setBackgroundColor(inactiveColor)
        }

        // Step 3 logic
        if (step >= 3) {
            binding.step3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor))
            binding.step3.setTextColor(activeText)
            binding.line2.setBackgroundColor(activeColor)
        } else {
            binding.step3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor))
            binding.step3.setTextColor(inactiveText)
            binding.line2.setBackgroundColor(inactiveColor)
        }
    }

    private fun submitBooking() {
        val studentNote = binding.etNotes.text.toString()
        val request = AppointmentRequest(slotId, studentNote)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        binding.btnConfirmBooking.isEnabled = false
        binding.btnConfirmBooking.text = "Submitting..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.bookAppointment(token, request)
                if (response.isSuccessful) {
                    showStep(4) // Show Success Screen
                } else {
                    Toast.makeText(this@BookAppointmentActivity, "Booking failed.", Toast.LENGTH_SHORT).show()
                    binding.btnConfirmBooking.isEnabled = true
                    binding.btnConfirmBooking.text = "Confirm Booking"
                }
            } catch (e: Exception) {
                Toast.makeText(this@BookAppointmentActivity, "Network error.", Toast.LENGTH_SHORT).show()
                binding.btnConfirmBooking.isEnabled = true
                binding.btnConfirmBooking.text = "Confirm Booking"
            }
        }
    }
}