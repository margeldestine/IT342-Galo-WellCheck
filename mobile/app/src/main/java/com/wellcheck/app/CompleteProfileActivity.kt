package com.wellcheck.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityCompleteProfileBinding
import com.wellcheck.app.network.CompleteProfileRequest
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Calendar

class CompleteProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompleteProfileBinding
    private var selectedBirthdate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        binding.tvSubtitle.text = "Welcome, $firstName! Please fill in your student details to continue."

        setupBirthdate()

        binding.btnCompleteProfile.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupBirthdate() {
        binding.etBirthdate.isFocusable = false
        binding.etBirthdate.isClickable = true
        binding.etBirthdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedBirthdate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.etBirthdate.setText(selectedBirthdate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }
    }

    private fun validateAndSubmit() {
        val studentId = binding.etStudentId.text.toString().trim()
        val program = binding.spinnerProgram.selectedItem.toString()
        val yearLevel = binding.spinnerYearLevel.selectedItem.toString()
        val gender = binding.spinnerGender.selectedItem.toString()

        when {
            studentId.isEmpty() ->
                showError("Student ID Number is required.")
            program == "Select Your Program" ->
                showError("Please select your enrolled program.")
            yearLevel == "Select" ->
                showError("Please select your year level.")
            gender == "Select" ->
                showError("Please select your gender.")
            selectedBirthdate.isEmpty() ->
                showError("Please select your birthdate.")
            else -> {
                hideError()
                submitProfile(CompleteProfileRequest(
                    studentIdNumber = studentId,
                    program = program,
                    yearLevel = yearLevel,
                    gender = gender,
                    birthdate = selectedBirthdate
                ))
            }
        }
    }

    private fun submitProfile(request: CompleteProfileRequest) {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.completeProfile(
                    "Bearer $token",
                    request
                )
                if (response.isSuccessful) {
                    runOnUiThread {
                        startActivity(Intent(
                            this@CompleteProfileActivity,
                            StudentDashboardActivity::class.java
                        ))
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError(errorBody ?: "Failed to complete profile. Please try again.")
                }
            } catch (e: Exception) {
                showError("Connection failed. Check your network.")
            }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        runOnUiThread {
            binding.btnCompleteProfile.isEnabled = !loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnCompleteProfile.text =
                if (loading) "Saving..." else "Complete Profile"
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