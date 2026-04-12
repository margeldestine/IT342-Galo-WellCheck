package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityLoginBinding
import com.wellcheck.app.network.LoginRequest
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                showError("Email address is required.")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Please enter a valid email address.")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showError("Password is required.")
                return@setOnClickListener
            }

            hideError()
            login(email, password)
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val user = response.body()

                    if (user?.status == "PENDING") {
                        showError("Your account is pending admin approval.")
                        setLoading(false)
                        return@launch
                    }

                    val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("accessToken", user?.accessToken)
                        .putString("role", user?.role)
                        .putString("email", user?.email)
                        .putString("firstName", user?.firstName ?: "")
                        .putString("lastName", user?.lastName ?: "")
                        .apply()

                    val firstName = user?.firstName ?: ""
                    val fullName = "$firstName ${user?.lastName ?: ""}".trim().ifEmpty { "User" }

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Welcome back, $fullName! 🎉", Toast.LENGTH_LONG).show()
                    }

                    when (user?.role) {
                        "STUDENT" -> startActivity(Intent(this@LoginActivity, StudentDashboardActivity::class.java))
                        "COUNSELOR" -> startActivity(Intent(this@LoginActivity, CounselorDashboardActivity::class.java))
                        else -> {
                            showError("Login failed. Unknown role.")
                            setLoading(false)
                            return@launch
                        }
                    }
                    finish()
                } else {
                    showError("Invalid email or password.")
                }
            } catch (e: Exception) {
                showError("Connection failed. Check your network.")
            }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignIn.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSignIn.text = if (loading) "Signing in..." else "Sign In"
    }

    private fun showError(msg: String) {
        runOnUiThread {
            binding.tvError.text = msg
            binding.tvError.visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}