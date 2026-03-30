package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellcheck.app.databinding.ActivityStudentDashboardBinding
import java.util.Calendar

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        val lastName = prefs.getString("lastName", "") ?: ""
        val fullName = "$firstName $lastName".trim().ifEmpty { "Student" }

        // Greeting
        val greeting = getGreeting()
        binding.tvGreeting.text = "$greeting, $fullName!"
        binding.tvGreetingSub.text = "Here's your wellness overview for today"

        // Topbar name + avatar initial
        binding.tvUserEmail.text = fullName
        val initial = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
        binding.tvAvatar.text = initial

        // Logout
        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
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
}