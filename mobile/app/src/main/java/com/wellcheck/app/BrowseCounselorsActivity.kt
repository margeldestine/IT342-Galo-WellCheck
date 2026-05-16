package com.wellcheck.app

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellcheck.app.adapter.CounselorAdapter
import com.wellcheck.app.databinding.ActivityBrowseCounselorsBinding
import com.wellcheck.app.network.CounselorListItem
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch

class BrowseCounselorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseCounselorsBinding
    private lateinit var adapter: CounselorAdapter

    private var allCounselors = listOf<CounselorListItem>()
    private var specializations = mutableListOf("All Specializations")

    private var currentSearchQuery = ""
    private var currentSpecialization = "All Specializations"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseCounselorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dmSerifItalic: Typeface? = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        binding.tvTitle.typeface = dmSerifItalic

        setupRecyclerView()
        setupBottomNav()
        setupSearchAndFilter()

        val token = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE).getString("token", "") ?: ""
        fetchCounselors("Bearer $token")
    }

    private fun setupRecyclerView() {
        adapter = CounselorAdapter(this, emptyList()) { counselor ->
            val sheet = SlotsBottomSheet(counselor) {
                // Refresh list after booking if needed
            }
            sheet.show(supportFragmentManager, "SlotsBottomSheet")
        }
        binding.rvCounselors.layoutManager = LinearLayoutManager(this)
        binding.rvCounselors.adapter = adapter
    }

    private fun setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim()
                applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.spinnerSpecialization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSpecialization = specializations[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyFilters() {
        val filtered = allCounselors.filter { c ->
            val fullName = "${c.firstName} ${c.lastName}".lowercase()
            val matchesSearch = fullName.contains(currentSearchQuery.lowercase())
            val matchesSpec = currentSpecialization == "All Specializations" || c.specialization == currentSpecialization
            matchesSearch && matchesSpec
        }
        adapter.updateData(filtered)
    }

    private fun fetchCounselors(token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselors(token)
                if (response.isSuccessful) {
                    allCounselors = (response.body() ?: emptyList())
                        .filter { it.availableSlots > 0 }

                    val specs = allCounselors.mapNotNull { it.specialization }.distinct().sorted()
                    specializations.clear()
                    specializations.add("All Specializations")
                    specializations.addAll(specs)

                    val spinnerAdapter = ArrayAdapter(
                        this@BrowseCounselorsActivity,
                        R.layout.item_spinner,
                        specializations
                    )
                    spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
                    binding.spinnerSpecialization.adapter = spinnerAdapter

                    applyFilters()
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        overridePendingTransition(0, 0)

        binding.navAppointments.setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
    }


}