package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellcheck.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val role = prefs.getString("role", null)
        if (token != null && role != null) {
            navigateByRole(role)
            return
        }

        binding.btnSignUpStudent.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

       binding.btnSignUpCounselor.setOnClickListener {
         startActivity(Intent(this, RegisterCounselorActivity::class.java))
       }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun navigateByRole(role: String) {
        when (role) {
            "STUDENT" -> startActivity(Intent(this, StudentDashboardActivity::class.java))
            "COUNSELOR" -> startActivity(Intent(this, CounselorDashboardActivity::class.java))
        }
        finish()
    }
}