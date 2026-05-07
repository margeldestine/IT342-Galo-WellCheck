package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OAuthCallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val token = uri.getQueryParameter("token")
        val role = uri.getQueryParameter("role")
        val email = uri.getQueryParameter("email")
        val firstName = uri.getQueryParameter("firstName")
        val lastName = uri.getQueryParameter("lastName")
        val isNewUser = uri.getQueryParameter("isNewUser") == "true"
        val status = uri.getQueryParameter("status")
        val error = uri.getQueryParameter("error")

        if (error != null || token == null) {
            // OAuth failed — go back to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Save token and user info to SharedPreferences
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("token", token)
            .putString("role", role)
            .putString("email", email)
            .putString("firstName", firstName ?: "")
            .putString("lastName", lastName ?: "")
            .putString("status", status ?: "")
            .apply()

        when {
            isNewUser && role == "STUDENT" -> {
                // New student — needs to complete profile
                startActivity(Intent(this, CompleteProfileActivity::class.java))
            }
            isNewUser && role == "COUNSELOR" -> {
                // New counselor — needs to complete profile
                // (we'll add this later)
                startActivity(Intent(this, CompleteProfileActivity::class.java))
            }
            role == "STUDENT" -> {
                startActivity(Intent(this, StudentDashboardActivity::class.java))
            }
            role == "COUNSELOR" -> {
                startActivity(Intent(this, CounselorDashboardActivity::class.java))
            }
            else -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        finish()
    }
}