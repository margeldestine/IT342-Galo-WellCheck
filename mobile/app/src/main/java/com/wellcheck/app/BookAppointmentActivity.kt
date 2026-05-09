package com.wellcheck.app

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.wellcheck.app.databinding.ActivityBookAppointmentBinding
import com.wellcheck.app.network.BookAppointmentRequest
import com.wellcheck.app.network.RetrofitClient
import com.wellcheck.app.network.StudentProfileResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAppointmentBinding

    private var slotId: Long = 0
    private var slotStart: String = ""
    private var slotEnd: String = ""
    private var counselorName: String = ""
    private var counselorSpec: String = ""

    private var currentStep = 1
    private var studentProfile: StudentProfileResponse? = null
    private var bookingNote: String = ""
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        slotId        = intent.getLongExtra("slot_id", 0)
        slotStart     = intent.getStringExtra("slot_start") ?: ""
        slotEnd       = intent.getStringExtra("slot_end") ?: ""
        counselorName = intent.getStringExtra("counselor_name") ?: ""
        counselorSpec = intent.getStringExtra("counselor_spec") ?: ""

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        token     = prefs.getString("token", "") ?: ""

        val dmSerif = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvMainTitle.typeface = dmSerif
        binding.tvMainTitle.text    = "Book Appointment"
        binding.tvMainSubtitle.text = "With $counselorName on ${formatShortDate(slotStart)} at ${formatTime(slotStart)}"

        binding.btnBack.setOnClickListener { finish() }

        // Show step 1 skeleton immediately, then fill once profile loads
        showStep(1)
        fetchStudentProfile()
    }

    // ── Step navigation ───────────────────────────────────────────────────────

    private fun showStep(step: Int) {
        currentStep = step
        updateStepIndicator(step)

        binding.layoutStep1.root.visibility   = if (step == 1) View.VISIBLE else View.GONE
        binding.layoutStep2.root.visibility   = if (step == 2) View.VISIBLE else View.GONE
        binding.layoutStep3.root.visibility   = if (step == 3) View.VISIBLE else View.GONE
        binding.layoutSuccess.root.visibility = if (step == 4) View.VISIBLE else View.GONE
        binding.layoutHeader.visibility       = if (step == 4) View.GONE else View.VISIBLE

        when (step) {
            1 -> setupStep1()
            2 -> setupStep2()
            3 -> setupStep3()
            4 -> setupSuccess()
        }
    }

    private fun updateStepIndicator(step: Int) {
        setStepCircle(binding.tvStep1, step, 1)
        setStepCircle(binding.tvStep2, step, 2)
        setStepCircle(binding.tvStep3, step, 3)
        binding.viewLine1.setBackgroundColor(
            if (step > 1) getColor(R.color.white) else 0x55FFFFFF.toInt()
        )
        binding.viewLine2.setBackgroundColor(
            if (step > 2) getColor(R.color.white) else 0x55FFFFFF.toInt()
        )
    }

    private fun setStepCircle(tv: TextView, currentStep: Int, thisStep: Int) {
        when {
            currentStep > thisStep -> {
                // Completed — green bg, white number
                tv.setBackgroundResource(R.drawable.bg_step_done)
                tv.setTextColor(0xFFFFFFFF.toInt())
            }
            currentStep == thisStep -> {
                // Active — white bg, green number
                tv.setBackgroundResource(R.drawable.bg_step_active)
                tv.setTextColor(getColor(R.color.green_dark))
            }
            else -> {
                // Future — transparent with white border
                tv.setBackgroundResource(R.drawable.bg_step_inactive)
                tv.setTextColor(0xAAFFFFFF.toInt())
            }
        }
    }

    // ── Step 1 ────────────────────────────────────────────────────────────────

    private fun setupStep1() {
        val s1 = binding.layoutStep1
        s1.tvStep1Title.typeface = ResourcesCompat.getFont(this, R.font.dm_serif_display_regular)
        s1.tvStep1Sub.typeface   = ResourcesCompat.getFont(this, R.font.inter)

        // Fill whatever we have so far (may be empty if profile not loaded yet)
        populateStep1()

        s1.btnStep1Change.setOnClickListener {
            Toast.makeText(this, "Go to your Profile to update details.", Toast.LENGTH_SHORT).show()
        }
        s1.btnStep1Confirm.setOnClickListener { showStep(2) }
    }

    private fun populateStep1() {
        val s1    = binding.layoutStep1
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)

        // Fallback: fill name from SharedPrefs immediately
        s1.etFirstName.setText(prefs.getString("firstName", ""))
        s1.etLastName.setText(prefs.getString("lastName", ""))

        // Once profile loads, fill everything (including name from nested user)
        studentProfile?.let { p ->
            s1.etStudentId.setText(p.studentIdNumber ?: "")
            s1.etProgram.setText(p.program ?: "")
            s1.etYearLevel.setText(p.yearLevel ?: "")
            s1.etGender.setText(p.gender ?: "")
            s1.etBirthdate.setText(p.birthdate ?: "")
            // firstName/lastName live inside the nested user object
            p.user?.firstName?.let { s1.etFirstName.setText(it) }
            p.user?.lastName?.let  { s1.etLastName.setText(it) }
        }
    }

    // ── Step 2 ────────────────────────────────────────────────────────────────

    private fun setupStep2() {
        val s2 = binding.layoutStep2
        s2.tvStep2Title.typeface = ResourcesCompat.getFont(this, R.font.dm_serif_display_regular)
        s2.tvStep2Sub.typeface   = ResourcesCompat.getFont(this, R.font.inter)

        val hasSchoolId = !studentProfile?.schoolIdPhotoUrl.isNullOrEmpty()
        s2.layoutIdOk.visibility      = if (hasSchoolId) View.VISIBLE else View.GONE
        s2.layoutIdMissing.visibility = if (hasSchoolId) View.GONE    else View.VISIBLE

        s2.etNote.setText(bookingNote)

        s2.btnStep2Back.setOnClickListener { showStep(1) }
        s2.btnStep2Review.isEnabled = hasSchoolId
        s2.btnStep2Review.setOnClickListener {
            bookingNote = s2.etNote.text.toString().trim()
            showStep(3)
        }
    }

    // ── Step 3 ────────────────────────────────────────────────────────────────

    private fun setupStep3() {
        val s3    = binding.layoutStep3
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        s3.tvStep3Title.typeface = ResourcesCompat.getFont(this, R.font.dm_serif_display_regular)
        s3.tvStep3Sub.typeface   = ResourcesCompat.getFont(this, R.font.inter)

        s3.tvReviewCounselor.text  = counselorName
        s3.tvReviewSpec.text       = counselorSpec
        s3.tvReviewDate.text       = formatDate(slotStart)
        s3.tvReviewTime.text       = "${formatTime(slotStart)} to ${formatTime(slotEnd)}"
        s3.tvReviewName.text       = "${prefs.getString("firstName","")} ${prefs.getString("lastName","")}"
        s3.tvReviewStudentId.text  = studentProfile?.studentIdNumber ?: ""

        s3.layoutReviewNote.visibility = if (bookingNote.isNotEmpty()) View.VISIBLE else View.GONE
        s3.tvReviewNote.text = bookingNote

        s3.tvBookingError.visibility = View.GONE
        s3.btnStep3Back.setOnClickListener { showStep(2) }
        s3.btnStep3Confirm.setOnClickListener { submitBooking() }
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    private fun submitBooking() {
        val s3 = binding.layoutStep3
        s3.btnStep3Confirm.isEnabled = false
        s3.btnStep3Confirm.text      = "Submitting…"
        s3.tvBookingError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.bookAppointment(
                    "Bearer $token",
                    BookAppointmentRequest(slotId = slotId, note = bookingNote.ifEmpty { null })
                )
                if (response.isSuccessful) {
                    showStep(4)
                } else {
                    s3.tvBookingError.text       = "Booking failed. Please try again."
                    s3.tvBookingError.visibility = View.VISIBLE
                    s3.btnStep3Confirm.isEnabled = true
                    s3.btnStep3Confirm.text      = "Confirm Booking"
                }
            } catch (e: Exception) {
                s3.tvBookingError.text       = "Something went wrong. Check your connection."
                s3.tvBookingError.visibility = View.VISIBLE
                s3.btnStep3Confirm.isEnabled = true
                s3.btnStep3Confirm.text      = "Confirm Booking"
            }
        }
    }

    // ── Success ───────────────────────────────────────────────────────────────

    private fun setupSuccess() {
        val ss = binding.layoutSuccess
        ss.tvSuccessTitle.typeface    = ResourcesCompat.getFont(this, R.font.dm_serif_display_regular)
        ss.tvSuccessSubtitle.text     = "Your appointment request has been sent to $counselorName. You'll be notified once it's confirmed."
        ss.tvSuccessCounselor.text    = counselorName
        ss.tvSuccessDate.text         = formatDate(slotStart)
        ss.tvSuccessTime.text         = "${formatTime(slotStart)} to ${formatTime(slotEnd)}"

        ss.btnSuccessViewApts.setOnClickListener { finish() }
        ss.btnSuccessDashboard.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            finish()
        }
    }

    // ── Fetch profile ─────────────────────────────────────────────────────────

    private fun fetchStudentProfile() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getStudentProfile("Bearer $token")
                if (response.isSuccessful) {
                    studentProfile = response.body()
                    // Always repopulate Step 1 once data arrives, regardless of current step
                    if (currentStep == 1) populateStep1()
                }
            } catch (e: Exception) {
                // Silently fail — fields stay with SharedPrefs data
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatTime(dt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val d   = sdf.parse(dt)
            SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(d ?: return dt)
        } catch (e: Exception) { dt }
    }

    private fun formatShortDate(dt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val d   = sdf.parse(dt)
            SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(d ?: return dt)
        } catch (e: Exception) { dt }
    }

    private fun formatDate(dt: String): String {
        return try {
            val sdf     = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val d       = sdf.parse(dt)
            val date    = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(d ?: return dt)
            val weekday = SimpleDateFormat("EEEE", Locale.ENGLISH).format(d)
            "$date ($weekday)"
        } catch (e: Exception) { dt }
    }
}