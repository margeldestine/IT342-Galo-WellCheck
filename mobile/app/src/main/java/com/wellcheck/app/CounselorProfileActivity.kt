package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wellcheck.app.databinding.ActivityCounselorProfileBinding
import com.wellcheck.app.network.*
import kotlinx.coroutines.launch

class CounselorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorProfileBinding
    private var token: String = ""
    private var credentials = mutableListOf<CredentialItemRequest>()
    private var selectedDays = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        val dmSerifItalic = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvTitle.typeface = dmSerifItalic

        setupBottomNavigation()
        fetchProfile()

        binding.btnSaveBasic.setOnClickListener { saveBasicInfo() }
        binding.btnSaveCredentials.setOnClickListener { saveCredentials() }
        binding.btnSaveAvailability.setOnClickListener { saveAvailability() }
        binding.btnAddCredential.setOnClickListener { addCredentialRow() }

        setupDayToggles()
    }

    private fun fetchProfile() {
        binding.progressBar.visibility = View.VISIBLE

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val savedFirstName = prefs.getString("firstName", "") ?: ""
        val savedLastName  = prefs.getString("lastName",  "") ?: ""
        val savedEmail     = prefs.getString("email",     "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyCounselorProfile("Bearer $token")
                if (response.isSuccessful) {
                    val profile = response.body() ?: return@launch
                    runOnUiThread { populateUI(profile, savedFirstName, savedLastName, savedEmail) }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CounselorProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorProfileActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread { binding.progressBar.visibility = View.GONE }
            }
        }
    }

    private fun populateUI(
        profile: CounselorProfileResponse,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val first = firstName.firstOrNull()?.uppercaseChar() ?: ""
        val last  = lastName.firstOrNull()?.uppercaseChar()  ?: ""

        binding.tvProfileName.text         = "$firstName $lastName"
        binding.tvEmployeeId.text          = "ID: ${profile.employeeNumber ?: "—"}"
        binding.tvSpecializationBadge.text = profile.specialization ?: "—"
        binding.tvRating.text              = String.format("%.1f", profile.averageRating)
        binding.tvReviewCount.text         = "${profile.ratingCount} reviews"
        renderStars(profile.averageRating)

        if (!profile.profilePhoto.isNullOrBlank()) {
            binding.ivProfilePhoto.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
            binding.ivProfilePhoto.clipToOutline = true
            binding.ivProfilePhoto.visibility = View.VISIBLE
            binding.tvProfileInitials.visibility = View.GONE
            Glide.with(this)
                .load(profile.profilePhoto)
                .circleCrop()
                .placeholder(R.drawable.circle_avatar_bg)
                .error(R.drawable.circle_avatar_bg)
                .into(binding.ivProfilePhoto)
        } else {
            binding.ivProfilePhoto.visibility = View.GONE
            binding.tvProfileInitials.visibility = View.VISIBLE
            binding.tvProfileInitials.text = "$first$last"
        }

        binding.etEmail.setText(email)
        binding.etFirstName.setText(firstName)
        binding.etLastName.setText(lastName)

        binding.etSpecialization.setText(profile.specialization)
        binding.etYearsExperience.setText(profile.yearsExperience?.toString() ?: "")
        binding.etLicenseNumber.setText(profile.licenseNumber ?: "")
        binding.etBio.setText(profile.bio ?: "")

        // Parse credentials from "Title|Year" strings
        credentials = profile.credentialEntries.map { entry ->
            val parts = entry.split("|")
            CredentialItemRequest(
                title = parts.getOrElse(0) { "" }.trim(),
                year  = parts.getOrElse(1) { "" }.trim()
            )
        }.toMutableList()
        renderCredentials()

        selectedDays = profile.availableDays.toMutableSet()
        updateDayToggleUI()
    }

    private fun renderStars(rating: Double) {
        listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
            .forEachIndexed { index, star ->
                star.setImageResource(
                    if (index + 1 <= rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
                )
            }
    }

    private fun renderCredentials() {
        binding.credentialContainer.removeAllViews()
        credentials.forEachIndexed { index, cred ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_credential_row, binding.credentialContainer, false)

            row.findViewById<TextView>(R.id.tvCredentialTitle).text =
                cred.title.ifBlank { "Degree ${index + 1}" }
            row.findViewById<TextView>(R.id.tvCredentialYear).text = cred.year

            row.findViewById<View>(R.id.btnRemoveCredential).setOnClickListener {
                credentials.removeAt(index)
                renderCredentials()
            }

            binding.credentialContainer.addView(row)
        }
    }

    private fun addCredentialRow() {
        val title = binding.etNewCredentialTitle.text.toString().trim()
        val year  = binding.etNewCredentialYear.text.toString().trim()

        if (title.isBlank()) {
            Toast.makeText(this, "Please enter a degree or certificate name.", Toast.LENGTH_SHORT).show()
            return
        }

        credentials.add(CredentialItemRequest(title, year))
        binding.etNewCredentialTitle.setText("")
        binding.etNewCredentialYear.setText("")
        renderCredentials()
    }

    private fun setupDayToggles() {
        mapOf(
            "Mon" to binding.btnMon,
            "Tue" to binding.btnTue,
            "Wed" to binding.btnWed,
            "Thu" to binding.btnThu,
            "Fri" to binding.btnFri
        ).forEach { (day, btn) ->
            btn.setOnClickListener {
                if (selectedDays.contains(day)) selectedDays.remove(day)
                else selectedDays.add(day)
                updateDayToggleUI()
            }
        }
    }

    private fun updateDayToggleUI() {
        mapOf(
            "Mon" to binding.btnMon,
            "Tue" to binding.btnTue,
            "Wed" to binding.btnWed,
            "Thu" to binding.btnThu,
            "Fri" to binding.btnFri
        ).forEach { (day, btn) ->
            if (selectedDays.contains(day)) {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#1B3A2D")
                )
                btn.setTextColor(android.graphics.Color.WHITE)
            } else {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#F3F4F6")
                )
                btn.setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
            }
        }
    }

    private fun buildUpdateRequest(): UpdateCounselorProfileRequest {
        return UpdateCounselorProfileRequest(
            specialization  = binding.etSpecialization.text.toString().trim(),
            bio             = binding.etBio.text.toString().trim(),
            yearsExperience = binding.etYearsExperience.text.toString().trim().toIntOrNull(),
            licenseNumber   = binding.etLicenseNumber.text.toString().trim(),
            availableDays   = selectedDays.toList(),
            credentials     = credentials
        )
    }

    private fun saveBasicInfo() {
        val specialization = binding.etSpecialization.text.toString().trim()
        if (specialization.isBlank()) {
            Toast.makeText(this, "Specialization is required.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateCounselorProfile(
                    "Bearer $token", buildUpdateRequest()
                )
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CounselorProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        binding.tvSpecializationBadge.text = specialization
                    } else {
                        val errBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@CounselorProfileActivity, "Update failed: $errBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveCredentials() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateCounselorProfile(
                    "Bearer $token", buildUpdateRequest()
                )
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CounselorProfileActivity, "Credentials saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@CounselorProfileActivity, "Failed to save credentials: $errBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveAvailability() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateCounselorProfile(
                    "Bearer $token", buildUpdateRequest()
                )
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CounselorProfileActivity, "Availability saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@CounselorProfileActivity, "Failed to save availability: $errBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CounselorProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            startActivity(Intent(this, CounselorDashboardActivity::class.java))
            finish(); overridePendingTransition(0, 0)
        }
        binding.navManageSlots.setOnClickListener {
            startActivity(Intent(this, CounselorManageSlotsActivity::class.java))
            finish(); overridePendingTransition(0, 0)
        }
        binding.navRequests.setOnClickListener {
            startActivity(Intent(this, CounselorRequestsActivity::class.java))
            finish(); overridePendingTransition(0, 0)
        }
        binding.navProfile.setOnClickListener { }
    }
}