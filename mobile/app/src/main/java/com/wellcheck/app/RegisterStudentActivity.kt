package com.wellcheck.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityRegisterStudentBinding
import com.wellcheck.app.network.RetrofitClient
import com.wellcheck.app.network.StudentRegisterRequest
import kotlinx.coroutines.launch
import java.util.Calendar
import com.wellcheck.app.network.NetworkConfig

class RegisterStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterStudentBinding
    private var selectedBirthdate = ""
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupBirthdate()
        setupPasswordToggle()
        setupClickListeners()
    }

    private fun setupSpinners() {

        val programs = resources.getStringArray(R.array.programs)
        val programAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, programs)
        binding.spinnerProgram.adapter = programAdapter


        val yearLevels = resources.getStringArray(R.array.year_levels)
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearLevels)
        binding.spinnerYearLevel.adapter = yearAdapter


        val genders = resources.getStringArray(R.array.genders)
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = genderAdapter
    }

    private fun setupBirthdate() {
        binding.etBirthdate.isFocusable = false
        binding.etBirthdate.isClickable = true
        binding.etBirthdate.setOnClickListener { showDatePicker() }
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

        binding.btnGoogle.setOnClickListener {
            val url = "${NetworkConfig.OAUTH_BASE_URL}/oauth2/authorization/google" +
                    "?redirect_uri=wellcheck://callback" +
                    "&prompt=select_account"

            val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                .setShowTitle(false)
                .build()

            intent.launchUrl(this, android.net.Uri.parse(url))
        }

        binding.btnCreateAccount.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun validateAndRegister() {
        val studentId = binding.etStudentId.text.toString().trim()
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val program = binding.spinnerProgram.selectedItem.toString()
        val yearLevel = binding.spinnerYearLevel.selectedItem.toString()
        val gender = binding.spinnerGender.selectedItem.toString()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            studentId.isEmpty() ->
                showError("Student ID Number is required.")
            !validateStudentId(studentId) ->
                showError("Student ID must follow format 0000-00000 or 00-0000-000.")
            firstName.isEmpty() ->
                showError("First name is required.")
            lastName.isEmpty() ->
                showError("Last name is required.")
            program == "Select Your Program" ->
                showError("Please select your enrolled program.")
            yearLevel == "Select" ->
                showError("Please select your year level.")
            gender == "Select" ->
                showError("Please select your gender.")
            selectedBirthdate.isEmpty() ->
                showError("Please select your birthdate.")
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
                    StudentRegisterRequest(
                        studentIdNumber = studentId,
                        firstName = firstName,
                        lastName = lastName,
                        program = program,
                        yearLevel = yearLevel,
                        gender = gender,
                        birthdate = selectedBirthdate,
                        email = email,
                        password = password
                    )
                )
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedBirthdate = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
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

    private fun register(request: StudentRegisterRequest) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registerStudent(request)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterStudentActivity,
                            "Account created! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(
                            Intent(this@RegisterStudentActivity, LoginActivity::class.java)
                        )
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
        runOnUiThread {
            binding.btnCreateAccount.isEnabled = !loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnCreateAccount.text =
                if (loading) "Creating Account..." else "Create Account"
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