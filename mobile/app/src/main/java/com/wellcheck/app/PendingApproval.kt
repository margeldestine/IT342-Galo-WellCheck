package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellcheck.app.databinding.ActivityPendingApprovalBinding

class PendingApprovalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingApprovalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        populateUserInfo()
        setupClickListeners()
    }

    private fun populateUserInfo() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        val email = prefs.getString("email", "") ?: ""

        binding.tvSubtitle.text =
            "Hi $firstName! Your counselor account has been submitted successfully."
        binding.tvEmail.text = email
    }

    private fun setupClickListeners() {
        binding.btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}