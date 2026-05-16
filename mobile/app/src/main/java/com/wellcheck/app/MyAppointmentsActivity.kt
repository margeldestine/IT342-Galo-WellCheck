package com.wellcheck.app

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellcheck.app.adapter.AppointmentAdapter
import com.wellcheck.app.databinding.ActivityMyAppointmentsBinding
import com.wellcheck.app.network.AppointmentResponse
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class MyAppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAppointmentsBinding
    private lateinit var adapter: AppointmentAdapter

    private var allAppointments = listOf<AppointmentResponse>()
    private var currentFilter = "ALL"
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
            .getString("token", "") ?: ""

        val dmSerif: Typeface? = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvTitle.typeface = dmSerif

        setupRecyclerView()
        setupFilterTabs()
        setupBottomNav()
        fetchAppointments()
    }

    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(this, emptyList()) { appointment ->
            showCancelDialog(appointment)
        }
        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter
    }

    private fun showCancelDialog(appointment: AppointmentResponse) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment? This action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelAppointment(appointment.id)
            }
            .setNegativeButton("Keep it", null)
            .show()
    }

    private fun cancelAppointment(id: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.cancelAppointment("Bearer $token", id)
                if (response.isSuccessful) {
                    fetchAppointments() // Refresh list
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    private fun setupFilterTabs() {
        val tabs = listOf(
            binding.tabAll       to "ALL",
            binding.tabPending   to "PENDING",
            binding.tabConfirmed to "CONFIRMED",
            binding.tabRejected  to "REJECTED",
            binding.tabCancelled to "CANCELLED"
        )

        // Set all inactive first
        tabs.forEach { (tab, _) -> setTabInactive(tab) }

        tabs.forEach { (tab, filter) ->
            tab.setOnClickListener {
                currentFilter = filter
                tabs.forEach { (t, _) -> setTabInactive(t) }
                setTabActive(tab)
                applyFilter()
            }
        }
        setTabActive(binding.tabAll)
    }

    private fun setTabActive(tab: com.google.android.material.button.MaterialButton) {
        tab.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.green_dark))
        tab.setTextColor(getColor(R.color.white))
    }

    private fun setTabInactive(tab: com.google.android.material.button.MaterialButton) {
        tab.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE5E7EB.toInt())
        tab.setTextColor(0xFF6B7280.toInt())
    }

    private fun applyFilter() {
        val filtered = if (currentFilter == "ALL") allAppointments
        else allAppointments.filter { it.status.uppercase() == currentFilter }

        if (filtered.isEmpty()) {
            binding.rvAppointments.visibility = View.GONE
            binding.layoutEmpty.visibility    = View.VISIBLE
        } else {
            binding.rvAppointments.visibility = View.VISIBLE
            binding.layoutEmpty.visibility    = View.GONE
            adapter.updateData(filtered)
        }
    }

    private fun fetchAppointments() {
        binding.progressBar.visibility    = View.VISIBLE
        binding.rvAppointments.visibility = View.GONE
        binding.layoutEmpty.visibility    = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyAppointments("Bearer $token")
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    allAppointments = response.body() ?: emptyList()
                    applyFilter()
                } else {
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun setupBottomNav() {
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
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
            finish()
            overridePendingTransition(0, 0)

        }
    }
}