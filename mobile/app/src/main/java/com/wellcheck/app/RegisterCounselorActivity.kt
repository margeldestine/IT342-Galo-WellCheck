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
    }

    private fun validateAndRegister() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val employeeId = binding.etEmployeeId.text.toString().trim()
        val specialization = binding.spinnerSpecialization.selectedItem.toString()
        val bio = binding.etBio.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            firstName.isEmpty() ->
                showError("First name is required.")
            lastName.isEmpty() ->
                showError("Last name is required.")
            employeeId.isEmpty() ->
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
                        employeeNumber = employeeId,
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
        runOnUiThread {
            binding.tvError.visibility = View.GONE
            binding.tvSuccess.visibility = View.GONE
        }
    }
}