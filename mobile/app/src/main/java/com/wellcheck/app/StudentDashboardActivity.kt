package com.wellcheck.app

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wellcheck.app.databinding.ActivityStudentDashboardBinding
import com.wellcheck.app.network.RetrofitClient
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding

    private val quotes = listOf(
        Pair("You are enough just as you are.", "Meghan Markle"),
        Pair("Almost everything will work again if you unplug it for a few minutes, including you.", "Anne Lamott"),
        Pair("Take care of your body. It's the only place you have to live.", "Jim Rohn"),
        Pair("Self-care is not selfish. You cannot serve from an empty vessel.", "Eleanor Brown"),
        Pair("You don't have to be positive all the time. It's perfectly okay to feel sad.", "Lori Deschene")
    )
    private var quoteIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs     = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "") ?: ""
        val lastName  = prefs.getString("lastName",  "") ?: ""
        val token     = prefs.getString("token",     "") ?: ""

        // Apply DM Serif italic to greeting (same pattern as counselor dashboard)
        val dmSerifItalic: Typeface? = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvWelcome.typeface = dmSerifItalic

        // Header
        binding.tvWelcome.text = "${getGreeting()}, $firstName."
        binding.tvDate.text    = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
        binding.tvAvatar.text  = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()

        // Logout on avatar tap
        binding.tvAvatar.setOnClickListener { showLogoutDialog() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }

        // Quote
        showQuote(quoteIndex)
        binding.btnRefreshQuote.setOnClickListener {
            quoteIndex = (quoteIndex + 1) % quotes.size
            showQuote(quoteIndex)
        }

        // Mood
        setupMood()

        // Quick actions
        binding.btnBookAppointment.setOnClickListener {
            // startActivity(Intent(this, BookAppointmentActivity::class.java))
        }
        binding.btnViewAppointments.setOnClickListener {
            // startActivity(Intent(this, MyAppointmentsActivity::class.java))
        }

        // View all counselors
        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(this, BrowseCounselorsActivity::class.java))
        }

        // Bottom nav
        binding.navCounselors.setOnClickListener {
            startActivity(Intent(this, BrowseCounselorsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navAppointments.setOnClickListener {
            // startActivity(Intent(this, MyAppointmentsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        binding.navProfile.setOnClickListener {
            // startActivity(Intent(this, StudentProfileActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        fetchAppointments("Bearer $token")
        fetchCounselors("Bearer $token")
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log out") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        getSharedPreferences("wellcheck_prefs", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ── Mood ──────────────────────────────────────────────────────────────────

    private fun setupMood() {
        val moods = listOf(
            Pair(binding.moodAwful, "Awful"),
            Pair(binding.moodLow,   "Low"),
            Pair(binding.moodOkay,  "Okay"),
            Pair(binding.moodGood,  "Good"),
            Pair(binding.moodGreat, "Great")
        )

        moods.forEach { (view, mood) ->
            view.setOnClickListener {
                moods.forEach { (v, _) -> v.setBackgroundResource(R.drawable.bg_mood_unselected) }
                view.setBackgroundResource(R.drawable.bg_mood_selected)
                showMoodResponse(mood)
            }
        }

        binding.btnCloseMood.setOnClickListener {
            binding.layoutMoodResponse.visibility = View.GONE
            moods.forEach { (v, _) -> v.setBackgroundResource(R.drawable.bg_mood_unselected) }
        }
    }

    private fun showMoodResponse(mood: String) {
        binding.layoutMoodResponse.visibility = View.VISIBLE

        when (mood) {
            "Awful", "Low" -> {
                binding.tvMoodMessage.text       = "We're sorry to hear that. It's okay not to be okay."
                binding.tvMoodSub.text           = "Would you like to talk to a counselor today?"
                binding.tvMoodSub.visibility     = View.VISIBLE
                binding.btnMoodAction.text       = "Book a session"
                binding.btnMoodAction.visibility = View.VISIBLE
                binding.btnMoodAction.setOnClickListener {
                    // startActivity(Intent(this@StudentDashboardActivity, BookAppointmentActivity::class.java))
                }
            }
            "Okay" -> {
                binding.tvMoodMessage.text       = "Hang in there — you're doing okay, and that's enough."
                binding.tvMoodSub.text           = "A counselor is always here if you need to talk."
                binding.tvMoodSub.visibility     = View.VISIBLE
                binding.btnMoodAction.text       = "Browse counselors"
                binding.btnMoodAction.visibility = View.VISIBLE
                binding.btnMoodAction.setOnClickListener {
                    startActivity(Intent(this@StudentDashboardActivity, BrowseCounselorsActivity::class.java))
                }
            }
            "Good", "Great" -> {
                binding.tvMoodMessage.text       = "That's wonderful to hear! Keep taking care of yourself. \uD83D\uDC9A"
                binding.tvMoodSub.visibility     = View.GONE
                binding.btnMoodAction.visibility = View.GONE
            }
        }
    }

    // ── Quote ─────────────────────────────────────────────────────────────────

    private fun showQuote(index: Int) {
        val (q, a) = quotes[index]
        binding.tvQuote.text       = "\u201c$q\u201d"
        binding.tvQuoteAuthor.text = "\u2014 $a"
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    private fun fetchAppointments(token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyAppointments(token)
                if (response.isSuccessful) {
                    val upcoming = (response.body() ?: emptyList())
                        .filter { it.status == "CONFIRMED" || it.status == "PENDING" }
                        .sortedBy { it.startTime }
                        .firstOrNull()

                    runOnUiThread {
                        if (upcoming == null) {
                            binding.layoutNoAppointment.visibility   = View.VISIBLE
                            binding.layoutAppointmentData.visibility = View.GONE
                        } else {
                            binding.layoutNoAppointment.visibility   = View.GONE
                            binding.layoutAppointmentData.visibility = View.VISIBLE

                            val cFirst = upcoming.counselorFirstName ?: ""
                            val cLast  = upcoming.counselorLastName  ?: ""
                            binding.tvCounselorName.text = "$cFirst $cLast"
                            binding.tvCounselorSpec.text = upcoming.counselorSpecialization ?: ""

                            val photo = upcoming.counselorProfilePhoto
                            if (!photo.isNullOrEmpty()) {
                                binding.ivCounselorPhoto.visibility    = View.VISIBLE
                                binding.tvCounselorInitials.visibility = View.GONE
                                Glide.with(this@StudentDashboardActivity)
                                    .load(photo)
                                    .circleCrop()
                                    .into(binding.ivCounselorPhoto)
                            } else {
                                binding.ivCounselorPhoto.visibility    = View.GONE
                                binding.tvCounselorInitials.visibility = View.VISIBLE
                                binding.tvCounselorInitials.text =
                                    "${cFirst.firstOrNull() ?: ""}${cLast.firstOrNull() ?: ""}".uppercase()
                            }

                            try {
                                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    .parse(upcoming.startTime)
                                binding.tvAppointmentTime.text = parsed?.let {
                                    SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault()).format(it)
                                } ?: upcoming.startTime
                            } catch (e: Exception) {
                                binding.tvAppointmentTime.text = upcoming.startTime
                            }

                            binding.tvAppointmentStatus.text = upcoming.status
                            when (upcoming.status) {
                                "CONFIRMED" -> {
                                    binding.tvAppointmentStatus.setTextColor(getColor(R.color.green_dark))
                                    binding.tvAppointmentStatus.setBackgroundResource(R.drawable.bg_badge_confirmed)
                                }
                                "PENDING" -> {
                                    binding.tvAppointmentStatus.setTextColor(0xFFD97706.toInt())
                                    binding.tvAppointmentStatus.setBackgroundResource(R.drawable.bg_badge_pending)
                                }
                                "CANCELLED" -> {
                                    binding.tvAppointmentStatus.setTextColor(0xFFDC2626.toInt())
                                    binding.tvAppointmentStatus.setBackgroundResource(R.drawable.bg_badge_cancelled)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) { /* silently fail */ }
        }
    }

    // ── Counselors ────────────────────────────────────────────────────────────

    private fun fetchCounselors(token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselors(token)
                if (response.isSuccessful) {
                    val counselors = (response.body() ?: emptyList())
                        .filter { it.availableSlots > 0 }
                        .take(3)
                    runOnUiThread {
                        binding.layoutCounselors.removeAllViews()
                        counselors.forEachIndexed { index, c ->
                            val row = LayoutInflater.from(this@StudentDashboardActivity)
                                .inflate(R.layout.item_counselor_row, binding.layoutCounselors, false)

                            val first = c.firstName ?: ""
                            val last  = c.lastName  ?: ""
                            val ivPhoto    = row.findViewById<CircleImageView>(R.id.ivPhoto)
                            val tvInitials = row.findViewById<TextView>(R.id.tvInitials)

                            if (!c.profilePhoto.isNullOrEmpty()) {
                                ivPhoto.visibility    = View.VISIBLE
                                tvInitials.visibility = View.GONE
                                Glide.with(this@StudentDashboardActivity)
                                    .load(c.profilePhoto)
                                    .circleCrop()
                                    .into(ivPhoto)
                            } else {
                                ivPhoto.visibility    = View.GONE
                                tvInitials.visibility = View.VISIBLE
                                tvInitials.text =
                                    "${first.firstOrNull() ?: ""}${last.firstOrNull() ?: ""}".uppercase()
                            }

                            row.findViewById<TextView>(R.id.tvName).text = "$first $last"
                            row.findViewById<TextView>(R.id.tvSpec).text = c.specialization ?: ""

                            binding.layoutCounselors.addView(row)

                            if (index < counselors.size - 1) {
                                val divider = View(this@StudentDashboardActivity)
                                divider.layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1
                                )
                                divider.setBackgroundColor(getColor(R.color.border))
                                binding.layoutCounselors.addView(divider)
                            }
                        }
                    }
                }
            } catch (e: Exception) { /* silently fail */ }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11  -> "Good morning"
            in 12..17 -> "Good afternoon"
            else      -> "Good evening"
        }
    }
}