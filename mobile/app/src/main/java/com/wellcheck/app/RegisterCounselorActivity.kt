package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityRegisterCounselorBinding
import com.wellcheck.app.network.CounselorRegisterRequest
import com.wellcheck.app.network.NetworkConfig
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterCounselorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterCounselorBinding
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCounselorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordToggle()
        setupClickListeners()
    }

    private fun setupPasswordToggle() {
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
    }

    private fun setupClickListeners() {
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            validateAndRegister()
        }

        binding.btnGoogle.setOnClickListener {
            val url = "${NetworkConfig.OAUTH_BASE_URL}/oauth2/authorization/google" +
                    "?redirect_uri=wellcheck://callback" +
                    "&prompt=select_account" +
                    "&role=COUNSELOR"

            val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                .setShowTitle(false)
                .build()

            intent.launchUrl(this@RegisterCounselorActivity, android.net.Uri.parse(url))
        }
    }

    private fun validateAndRegister() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val employeeNumber = binding.etEmployeeNumber.text.toString().trim()
        val specialization = binding.spinnerSpecialization.selectedItem.toString()
        val bio = binding.etShortBio.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            firstName.isEmpty() ->
                showError("First name is required.")
            lastName.isEmpty() ->
                showError("Last name is required.")
            employeeNumber.isEmpty() ->
                showError("Employee number is required.")
            specialization == "Select Specialization" ->
                showError("Please select your specialization.")
            email.isEmpty() ->
                showError("Email address is required.")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                showError("Please enter a valid email address.")
            password.isEmpty() ->
                showError("Password is required.")
            password.length < 8 ->
                showError("Password must be at least 8 characters.")
            else -> {
                hideError()
                register(
                    CounselorRegisterRequest(
                        firstName = firstName,
                        lastName = lastName,
                        employeeNumber = employeeNumber,
                        specialization = specialization,
                        bio = bio,
                        email = email,
                        password = password
                    )
                )
            }
        }
    }

    private fun register(request: CounselorRegisterRequest) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registerCounselor(request)
                if (response.isSuccessful) {

                    getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("firstName", binding.etFirstName.text.toString().trim())
                        .putString("email", binding.etEmail.text.toString().trim())
                        .apply()

                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterCounselorActivity,
                            "Registration submitted! Awaiting admin approval.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@RegisterCounselorActivity, PendingApprovalActivity::class.java))
                        finishAffinity()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError(errorBody ?: "Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                showError("Connection failed. Check your network.")
            }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        runOnUiThread {
            binding.btnSubmit.isEnabled = !loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSubmit.text = if (loading) "Submitting..." else "Submit Registration"
        }
    }

    private fun showError(msg: String) {
        runOnUiThread {
            binding.tvError.text = msg
            binding.tvError.visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        runOnUiThread {
            binding.tvError.visibility = View.GONE
        }
    }
}