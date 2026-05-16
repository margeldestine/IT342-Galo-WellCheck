package com.wellcheck.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Typeface
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wellcheck.app.databinding.ActivityStudentProfileBinding
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding
    private var token = ""
    private var firstName = ""
    private var lastName = ""
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.imgSchoolId.setImageURI(it)
            binding.imgSchoolId.visibility = View.VISIBLE
            binding.btnUploadId.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""
        firstName = prefs.getString("firstName", "") ?: ""
        lastName = prefs.getString("lastName", "") ?: ""

        setupNav()
        applyFonts()
        fetchProfile()

        // btnUploadEmpty is a TextView in the XML used as a tap target
        binding.btnUploadEmpty.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnUploadId.setOnClickListener {
            uploadSchoolId()
        }
    }

    private fun applyFonts() {
        val dmSerifItalic: Typeface? = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvPageTitle.typeface = dmSerifItalic
    }

    private fun fetchProfile() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollContent.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getStudentProfile("Bearer $token")
                binding.progressBar.visibility = View.GONE
                binding.scrollContent.visibility = View.VISIBLE

                if (response.isSuccessful) {
                    val profile = response.body()

                    val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                    binding.tvInitials.text = initials
                    binding.tvFullName.text = "$firstName $lastName"
                    binding.tvStudentId.text = "ID: ${profile?.studentIdNumber ?: "—"}"

                    binding.tvPillProgram.text = profile?.program ?: ""
                    binding.tvPillYear.text = profile?.yearLevel ?: ""
                    binding.tvPillGender.text = profile?.gender ?: ""

                    binding.tvBirthdate.text = formatBirthdate(profile?.birthdate)

                    binding.bannerNoId.visibility =
                        if (profile?.schoolIdPhotoUrl.isNullOrBlank()) View.VISIBLE else View.GONE

                    binding.etStudentId.setText(profile?.studentIdNumber ?: "")
                    binding.etFirstName.setText(firstName)
                    binding.etLastName.setText(lastName)
                    binding.etProgram.setText(profile?.program ?: "")
                    binding.etYearLevel.setText(profile?.yearLevel ?: "")
                    binding.etGender.setText(profile?.gender ?: "")
                    binding.etBirthdate.setText(profile?.birthdate ?: "")

                    if (!profile?.schoolIdPhotoUrl.isNullOrBlank()) {
                        binding.imgSchoolId.visibility = View.VISIBLE
                        Glide.with(this@StudentProfileActivity)
                            .load(profile?.schoolIdPhotoUrl)
                            .into(binding.imgSchoolId)
                        binding.btnChangePhoto.visibility = View.VISIBLE
                        binding.btnUploadEmpty.visibility = View.GONE
                    } else {
                        binding.imgSchoolId.visibility = View.GONE
                        binding.btnChangePhoto.visibility = View.GONE
                        binding.btnUploadEmpty.visibility = View.VISIBLE
                    }
                    binding.btnUploadId.visibility = View.GONE

                } else {
                    // Fallback to SharedPrefs
                    val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                    binding.tvInitials.text = initials
                    binding.tvFullName.text = "$firstName $lastName"
                    binding.tvStudentId.text = "ID: —"
                    binding.etFirstName.setText(firstName)
                    binding.etLastName.setText(lastName)
                    binding.btnUploadEmpty.visibility = View.VISIBLE
                    binding.btnChangePhoto.visibility = View.GONE
                    binding.imgSchoolId.visibility = View.GONE
                    binding.btnUploadId.visibility = View.GONE
                    binding.bannerNoId.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.scrollContent.visibility = View.VISIBLE
                val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                binding.tvInitials.text = initials
                binding.tvFullName.text = "$firstName $lastName"
                binding.etFirstName.setText(firstName)
                binding.etLastName.setText(lastName)
                binding.btnUploadEmpty.visibility = View.VISIBLE
                binding.btnChangePhoto.visibility = View.GONE
                binding.imgSchoolId.visibility = View.GONE
                binding.btnUploadId.visibility = View.GONE
            }
        }
    }

    private fun uploadSchoolId() {
        val uri = selectedImageUri ?: return
        binding.btnUploadId.isEnabled = false
        binding.btnUploadId.text = "Uploading..."

        lifecycleScope.launch {
            try {
                val stream = contentResolver.openInputStream(uri) ?: return@launch
                val file = File(cacheDir, "school_id_upload.jpg")
                file.outputStream().use { stream.copyTo(it) }

                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val response = RetrofitClient.instance.uploadSchoolId("Bearer $token", part)
                if (response.isSuccessful) {
                    Toast.makeText(this@StudentProfileActivity, "School ID uploaded successfully!", Toast.LENGTH_SHORT).show()
                    binding.btnUploadId.visibility = View.GONE
                    binding.btnChangePhoto.visibility = View.VISIBLE
                    binding.btnUploadEmpty.visibility = View.GONE
                    binding.bannerNoId.visibility = View.GONE
                    selectedImageUri = null
                } else {
                    Toast.makeText(this@StudentProfileActivity, "Upload failed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentProfileActivity, "Upload error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            binding.btnUploadId.isEnabled = true
            binding.btnUploadId.text = "Upload"
        }
    }

    private fun formatBirthdate(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val d = sdf.parse(raw)
            SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(d ?: return raw)
        } catch (e: Exception) { raw }
    }

    private fun setupNav() {
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navCounselors.setOnClickListener {
            startActivity(Intent(this, BrowseCounselorsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navAppointments.setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navProfile.setOnClickListener { /* already here */ }
    }
}