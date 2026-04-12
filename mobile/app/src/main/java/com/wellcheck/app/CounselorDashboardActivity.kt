package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityCounselorDashboardBinding
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Calendar

class CounselorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        val lastName = prefs.getString("lastName", "") ?: ""
        val fullName = "$firstName $lastName".trim().ifEmpty { "Counselor" }

        binding.tvUserName.text = fullName
        val initial = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        binding.tvAvatar.text = initial

        val greeting = getGreeting()
        binding.tvGreeting.text = "$greeting, $firstName!"

        fetchDashboardStats()

        binding.cardCreateSlot.setOnClickListener {
            startActivity(Intent(this, ManageSlotsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchDashboardStats() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val tokenValue = prefs.getString("accessToken", "") ?: ""

        if (tokenValue.isEmpty()) {
            Log.e("API_ERROR", "No access token found. Redirecting to login.")
            return
        }

        val token = "Bearer $tokenValue"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMySlots(token)
                if (response.isSuccessful) {
                    val slots = response.body() ?: emptyList()

                    Log.d("DATA_CHECK", "Slots retrieved: ${slots.size}")

                    val totalSlotsCount = slots.size
                    val confirmedSlotsCount = slots.count { it.status.uppercase() == "BOOKED" }

                    // Update UI
                    binding.tvTotalCount.text = totalSlotsCount.toString()
                    binding.tvConfirmedCount.text = confirmedSlotsCount.toString()
                } else {
                    Log.e("API_ERROR", "Failed to fetch stats. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Dashboard fetch failed: ${e.message}")
            }
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardStats()
    }
}