package com.wellcheck.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OAuthCallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent.data

        if (uri != null && uri.scheme == "wellcheck" && uri.host == "callback") {
            val token = uri.getQueryParameter("token") ?: ""
            val email = uri.getQueryParameter("email") ?: ""
            val firstName = uri.getQueryParameter("firstName") ?: ""
            val lastName = uri.getQueryParameter("lastName") ?: ""
            val role = uri.getQueryParameter("role") ?: "STUDENT"
            val status = uri.getQueryParameter("status") ?: "ACTIVE"
            val isNewUser = uri.getQueryParameter("isNewUser")?.toBoolean() ?: false

            val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
            prefs.edit().apply {
                putString("token", token)
                putString("email", email)
                putString("firstName", firstName)
                putString("lastName", lastName)
                putString("role", role)
                putString("status", status)
                putBoolean("isNewUser", isNewUser)
                apply()
            }

            val nextIntent = when {
                status == "PENDING" -> {
                    Intent(this, PendingApprovalActivity::class.java)
                }
                isNewUser && role == "COUNSELOR" -> {
                    Intent(this, CompleteCounselorProfileActivity::class.java)
                }
                isNewUser -> {
                    Intent(this, CompleteProfileActivity::class.java)
                }
                role == "COUNSELOR" -> {
                    Intent(this, CounselorDashboardActivity::class.java)
                }
                else -> {
                    Intent(this, StudentDashboardActivity::class.java)
                }
            }

            startActivity(nextIntent)
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}