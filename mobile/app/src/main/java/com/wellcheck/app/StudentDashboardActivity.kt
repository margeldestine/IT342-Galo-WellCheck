package com.wellcheck.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityStudentDashboardBinding
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        val lastName = prefs.getString("lastName", "") ?: ""

        binding.tvStudentNameTop.text = "$firstName $lastName"
        binding.tvAvatar.text = firstName.firstOrNull()?.uppercase() ?: "S"

        val greetingPrefix = getGreetingPrefix()
        binding.tvGreeting.text = "$greetingPrefix, $firstName!"

        // Navigation Actions
        val toBrowse = { _: View -> startActivity(Intent(this, BrowseCounselorsActivity::class.java)) }
        binding.btnBrowseCounselors.setOnClickListener(toBrowse)
        binding.btnBookMain.setOnClickListener(toBrowse)

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Fetch the data!
        fetchUpcomingAppointment()
    }

    private fun fetchUpcomingAppointment() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyAppointments(token)
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()

                    val upcoming = appointments.firstOrNull { it.status == "CONFIRMED" || it.status == "PENDING" }

                    if (upcoming != null) {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.layoutConfirmedAppointment.visibility = View.VISIBLE

                        val counselorName = "${upcoming.counselorFirstName} ${upcoming.counselorLastName}"
                        binding.tvUpcomingCounselorName.text = counselorName
                        binding.tvUpcomingSpecialization.text = upcoming.counselorSpecialization
                        binding.tvCounselorAvatar.text = upcoming.counselorFirstName?.firstOrNull()?.uppercase() ?: "C"

                        binding.tvStatusBadge.text = upcoming.status
                        if (upcoming.status == "PENDING") {
                            binding.tvStatusBadge.setTextColor(Color.parseColor("#D97706"))
                            binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
                        } else {
                            binding.tvStatusBadge.setTextColor(Color.parseColor("#2D6A4F"))
                            binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F3E8"))
                        }

                        // Format Time
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val dateStart = inputFormat.parse(upcoming.startTime)
                        if (dateStart != null) {
                            binding.tvUpcomingDate.text = "📅 ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(dateStart)}"
                            binding.tvUpcomingTime.text = "🕒 ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(dateStart)}"
                        }
                    } else {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.layoutConfirmedAppointment.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Error: ${e.message}")
            }
        }
    }

    private fun getGreetingPrefix(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}