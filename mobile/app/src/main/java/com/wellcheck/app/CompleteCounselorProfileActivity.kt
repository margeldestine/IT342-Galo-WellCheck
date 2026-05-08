package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wellcheck.app.databinding.ActivityCompleteCounselorProfileBinding
import com.wellcheck.app.network.CompleteCounselorProfileRequest
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class CompleteCounselorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompleteCounselorProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteCounselorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        binding.tvSubtitle.text = "Welcome, $firstName! Please fill in your counselor details to continue."

        binding.btnCompleteProfile.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val employeeNumber = binding.etEmployeeNumber.text.toString().trim()
        val specialization = binding.spinnerSpecialization.selectedItem.toString()
        val shortBio = binding.etShortBio.text.toString().trim()

        when {
            employeeNumber.isEmpty() ->
                showError("Employee number is required.")
            specialization == "Select Specialization" ->
                showError("Please select your specialization.")
            shortBio.isEmpty() ->
                showError("Short bio is required.")
            else -> {
                hideError()
                submitProfile(
                    CompleteCounselorProfileRequest(
                        employeeNumber = employeeNumber,
                        specialization = specialization,
                        bio = shortBio
                    )
                )
            }
        }
    }

    private fun submitProfile(request: CompleteCounselorProfileRequest) {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.completeCounselorProfile(
                    "Bearer $token",
                    request
                )
                if (response.isSuccessful) {
                    // Backend returns a plain String, not a status object.
                    // Counselors always need admin approval after completing profile.
                    runOnUiThread {
                        startActivity(Intent(
                            this@CompleteCounselorProfileActivity,
                            PendingApprovalActivity::class.java
                        ))
                        finishAffinity()
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