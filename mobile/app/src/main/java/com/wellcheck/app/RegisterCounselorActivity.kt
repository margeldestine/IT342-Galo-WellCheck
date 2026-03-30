package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityRegisterCounselorBinding
import com.wellcheck.app.network.CounselorRegisterRequest
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterCounselorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterCounselorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCounselorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnSubmit.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val employeeId = binding.etEmployeeId.text.toString().trim()
            val specialization = binding.spinnerSpecialization.selectedItem.toString()
            val bio = binding.etBio.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validations
            if (firstName.isEmpty()) {
                showError("First name is required.")
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                showError("Last name is required.")
                return@setOnClickListener
            }

            if (employeeId.isEmpty()) {
                showError("Employee number is required.")
                return@setOnClickListener
            }

            if (specialization == "Select Specialization") {
                showError("Please select your specialization.")
                return@setOnClickListener
            }

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

            if (password.length < 8) {
                showError("Password must be at least 8 characters.")
                return@setOnClickListener
            }

            hideError()
            register(CounselorRegisterRequest(
                firstName = firstName,
                lastName = lastName,
                employeeNumber = employeeId,
                specialization = specialization,
                bio = bio,
                email = email,
                password = password
            ))
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun register(request: CounselorRegisterRequest) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registerCounselor(request)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterCounselorActivity,
                            "Registration submitted! Awaiting admin approval.",
                            Toast.LENGTH_LONG
                        ).show()
                        showSuccess("Registration submitted! Your account is pending admin approval.")
                        binding.btnSubmit.isEnabled = false
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
        binding.btnSubmit.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSubmit.text = if (loading) "Submitting..." else "Submit Registration"
    }

    private fun showError(msg: String) {
        runOnUiThread {
            binding.tvError.text = msg
            binding.tvError.visibility = View.VISIBLE
            binding.tvSuccess.visibility = View.GONE
        }
    }

    private fun showSuccess(msg: String) {
        runOnUiThread {
            binding.tvSuccess.text = msg
            binding.tvSuccess.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE
        }
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
        binding.tvSuccess.visibility = View.GONE
    }
}