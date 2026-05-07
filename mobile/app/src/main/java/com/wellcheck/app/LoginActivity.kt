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
import com.wellcheck.app.network.NetworkConfig

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

        var passwordVisible = false
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.etPassword.inputType = if (passwordVisible)
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(
                if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

        binding.btnGoogle.setOnClickListener {
            val url = "${NetworkConfig.OAUTH_BASE_URL}/oauth2/authorization/google" +
                    "?redirect_uri=wellcheck://callback"

            val headers = android.os.Bundle().apply {
                putString("ngrok-skip-browser-warning", "true" +
                        "&ngrok-skip-browser-warning=true")
            }

            val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                .setShowTitle(false)
                .build()

            intent.intent.putExtra(
                android.provider.Browser.EXTRA_HEADERS,
                headers
            )

            intent.launchUrl(this, android.net.Uri.parse(url))
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

                    // Save to SharedPreferences
                    val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("token", user?.accessToken)
                        .putString("role", user?.role)
                        .putString("email", user?.email)
                        .putString("firstName", user?.firstName ?: "")
                        .putString("lastName", user?.lastName ?: "")
                        .putString("profilePhoto", user?.profilePhoto ?: "")
                        .putString("specialization", user?.specialization ?: "")
                        .apply()

                    val firstName = user?.firstName ?: ""
                    val lastName = user?.lastName ?: ""
                    val fullName = "$firstName $lastName".trim().ifEmpty { "User" }

                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome back, $fullName! 🎉",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // Navigate based on role
                    when (user?.role) {
                        "STUDENT" -> startActivity(
                            Intent(this@LoginActivity, StudentDashboardActivity::class.java)
                        )
                        "COUNSELOR" -> startActivity(
                            Intent(this@LoginActivity, CounselorDashboardActivity::class.java)
                        )
                        else -> {
                            showError("Login failed. Please try again.")
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