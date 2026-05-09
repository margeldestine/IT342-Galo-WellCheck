package com.wellcheck.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wellcheck.app.databinding.ActivityCounselorProfileBinding
import com.wellcheck.app.network.*
import kotlinx.coroutines.launch

class CounselorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorProfileBinding
    private var token: String = ""
    private var credentials = mutableListOf<CredentialItem>()
    private var selectedDays = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

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
        val savedLastName = prefs.getString("lastName", "") ?: ""
        val savedEmail = prefs.getString("email", "") ?: ""

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

    private fun populateUI(profile: CounselorProfileResponse, firstName: String, lastName: String, email: String) {
        val first = firstName.firstOrNull()?.uppercaseChar() ?: ""
        val last = lastName.firstOrNull()?.uppercaseChar() ?: ""

        binding.tvProfileName.text = "$firstName $lastName"
        binding.tvEmployeeId.text = "ID: ${profile.employeeNumber ?: "—"}"
        binding.tvSpecializationBadge.text = profile.specialization ?: "—"
        binding.tvRating.text = String.format("%.1f", profile.averageRating)
        binding.tvReviewCount.text = "${profile.ratingCount} reviews"
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

        credentials = profile.credentialEntries.map { entry ->
            val parts = entry.split("|")
            CredentialItem(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
        }.toMutableList()
        renderCredentials()

        selectedDays = profile.availableDays.toMutableSet()
        updateDayToggleUI()
    }

    private fun renderStars(rating: Double) {
        val starViews = listOf(
            binding.star1, binding.star2, binding.star3,
            binding.star4, binding.star5
        )
        starViews.forEachIndexed { index, star ->
            val filled = index + 1 <= rating
            star.setImageResource(
                if (filled) R.drawable.ic_star_filled else R.drawable.ic_star_empty
            )
        }
    }

    private fun renderCredentials() {
        binding.credentialContainer.removeAllViews()
        credentials.forEachIndexed { index, cred ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_credential_row, binding.credentialContainer, false)

            row.findViewById<TextView>(R.id.tvCredentialTitle).text =
                if (cred.title.isNotBlank()) cred.title else "Degree ${index + 1}"
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
        val year = binding.etNewCredentialYear.text.toString().trim()

        if (title.isBlank()) {
            Toast.makeText(this, "Please enter a degree or certificate name.", Toast.LENGTH_SHORT).show()
            return
        }

        credentials.add(CredentialItem(title, year))
        binding.etNewCredentialTitle.setText("")
        binding.etNewCredentialYear.setText("")
        renderCredentials()
    }

    private fun setupDayToggles() {
        val dayButtons = mapOf(
            "Mon" to binding.btnMon,
            "Tue" to binding.btnTue,
            "Wed" to binding.btnWed,
            "Thu" to binding.btnThu,
            "Fri" to binding.btnFri
        )
        dayButtons.forEach { (day, btn) ->
            btn.setOnClickListener {
                if (selectedDays.contains(day)) selectedDays.remove(day)
                else selectedDays.add(day)
                updateDayToggleUI()
            }
        }
    }

    private fun updateDayToggleUI() {
        val dayButtons = mapOf(
            "Mon" to binding.btnMon,
            "Tue" to binding.btnTue,
            "Wed" to binding.btnWed,
            "Thu" to binding.btnThu,
            "Fri" to binding.btnFri
        )
        dayButtons.forEach { (day, btn) ->
            if (selectedDays.contains(day)) {
                btn.setBackgroundResource(R.drawable.tab_active_bg)
                btn.setTextColor(getColor(R.color.white))
            } else {
                btn.setBackgroundResource(R.drawable.tab_inactive_bg)
                btn.setTextColor(getColor(R.color.text_secondary))
            }
        }
    }

    private fun buildUpdateRequest(): UpdateCounselorProfileRequest {
        return UpdateCounselorProfileRequest(
            specialization = binding.etSpecialization.text.toString().trim(),
            bio = binding.etBio.text.toString().trim(),
            yearsExperience = binding.etYearsExperience.text.toString().trim().toIntOrNull(),
            licenseNumber = binding.etLicenseNumber.text.toString().trim(),
            availableDays = selectedDays.toList(),
            credentials = credentials
        )
    }

    private fun saveBasicInfo() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val specialization = binding.etSpecialization.text.toString().trim()

        if (firstName.isBlank() || lastName.isBlank() || specialization.isBlank()) {
            Toast.makeText(this, "First name, last name, and specialization are required.", Toast.LENGTH_SHORT).show()
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
                        binding.tvProfileName.text = "$firstName $lastName"
                        binding.tvSpecializationBadge.text = specialization
                        val f = firstName.firstOrNull()?.uppercaseChar() ?: ""
                        val l = lastName.firstOrNull()?.uppercaseChar() ?: ""
                        binding.tvProfileInitials.text = "$f$l"
                        getSharedPreferences("wellcheck_prefs", MODE_PRIVATE).edit()
                            .putString("firstName", firstName)
                            .putString("lastName", lastName)
                            .apply()
                    } else {
                        Toast.makeText(this@CounselorProfileActivity, "Update failed.", Toast.LENGTH_SHORT).show()
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
                    if (response.isSuccessful)
                        Toast.makeText(this@CounselorProfileActivity, "Credentials saved!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@CounselorProfileActivity, "Failed to save credentials.", Toast.LENGTH_SHORT).show()
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
                    if (response.isSuccessful)
                        Toast.makeText(this@CounselorProfileActivity, "Availability saved!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@CounselorProfileActivity, "Failed to save availability.", Toast.LENGTH_SHORT).show()
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
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navManageSlots.setOnClickListener {
            startActivity(Intent(this, CounselorManageSlotsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navRequests.setOnClickListener {
            startActivity(Intent(this, CounselorRequestsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navProfile.setOnClickListener { }
    }
}