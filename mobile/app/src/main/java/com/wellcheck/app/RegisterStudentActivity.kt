package com.wellcheck.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityRegisterStudentBinding
import com.wellcheck.app.network.RetrofitClient
import com.wellcheck.app.network.StudentRegisterRequest
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterStudentBinding
    private var selectedBirthdate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.etBirthdate.setOnClickListener {
            showDatePicker()
        }
        binding.etBirthdate.isFocusable = false
        binding.etBirthdate.isClickable = true

        binding.btnCreateAccount.setOnClickListener {
            val studentId = binding.etStudentId.text.toString().trim()
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val program = binding.spinnerProgram.selectedItem.toString()
            val yearLevel = binding.spinnerYearLevel.selectedItem.toString()
            val gender = binding.spinnerGender.selectedItem.toString()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validations
            if (studentId.isEmpty()) {
                showError("Student ID Number is required.")
                return@setOnClickListener
            }

            if (!validateStudentId(studentId)) {
                showError("Student ID must follow format 0000-00000 or 00-0000-000.")
                return@setOnClickListener
            }

            if (firstName.isEmpty()) {
                showError("First name is required.")
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                showError("Last name is required.")
                return@setOnClickListener
            }

            if (program == "Select Your Program") {
                showError("Please select your enrolled program.")
                return@setOnClickListener
            }

            if (yearLevel == "Select") {
                showError("Please select your year level.")
                return@setOnClickListener
            }

            if (gender == "Select") {
                showError("Please select your gender.")
                return@setOnClickListener
            }

            if (selectedBirthdate.isEmpty()) {
                showError("Please select your birthdate.")
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
            register(StudentRegisterRequest(
                studentIdNumber = studentId,
                firstName = firstName,
                lastName = lastName,
                program = program,
                yearLevel = yearLevel,
                gender = gender,
                birthdate = selectedBirthdate,
                email = email,
                password = password
            ))
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format as YYYY-MM-DD for backend
                selectedBirthdate = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                // Display as readable format
                binding.etBirthdate.setText(selectedBirthdate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun register(request: StudentRegisterRequest) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registerStudent(request)
                if (response.isSuccessful) {
                    runOnUiThread {
                        android.widget.Toast.makeText(
                            this@RegisterStudentActivity,
                            "Account created! Please log in.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@RegisterStudentActivity, LoginActivity::class.java))
                        finish()
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

    private fun validateStudentId(id: String): Boolean {
        val pattern1 = Regex("^\\d{4}-\\d{5}$")
        val pattern2 = Regex("^\\d{2}-\\d{4}-\\d{3}$")
        return pattern1.matches(id) || pattern2.matches(id)
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateAccount.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.text = if (loading) "Creating Account..." else "Create Account"
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