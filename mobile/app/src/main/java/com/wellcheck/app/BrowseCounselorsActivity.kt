package com.wellcheck.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellcheck.app.databinding.ActivityBrowseCounselorsBinding
import com.wellcheck.app.network.CounselorResponse
import com.wellcheck.app.network.RetrofitClient
import kotlinx.coroutines.launch
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BrowseCounselorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseCounselorsBinding
    private lateinit var adapter: CounselorAdapter
    private var allCounselors: List<CounselorResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseCounselorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFilters()
        fetchCounselors()
    }

    private fun setupRecyclerView() {
        adapter = CounselorAdapter(emptyList()) { counselor ->
            showSlotsDialog(counselor)
        }
        binding.rvCounselors.layoutManager = LinearLayoutManager(this)
        binding.rvCounselors.adapter = adapter
    }

    private fun setupFilters() {
        // Dropdown setup
        val specializations = arrayOf("All Specializations", "Mental Health", "Academic", "Career")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSpecialization.adapter = spinnerAdapter

        binding.spinnerSpecialization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Search bar setup
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterList() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val selectedSpec = binding.spinnerSpecialization.selectedItem.toString()

        val filtered = allCounselors.filter { counselor ->
            val matchesName = "${counselor.firstName} ${counselor.lastName}".lowercase().contains(query)
            val matchesSpec = selectedSpec == "All Specializations" || counselor.specialization.equals(selectedSpec, ignoreCase = true)
            matchesName && matchesSpec
        }
        adapter.updateList(filtered)
    }

    private fun fetchCounselors() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselors(token)
                if (response.isSuccessful) {
                    allCounselors = response.body() ?: emptyList()
                    filterList() // Applies initial filters and updates adapter
                } else {
                    Log.e("API_ERROR", "Failed to fetch counselors: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
            }
        }
    }

    private fun showSlotsDialog(counselor: CounselorResponse) {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselorAvailableSlots(token, counselor.id)
                if (response.isSuccessful) {
                    val slots = response.body() ?: emptyList()

                    if (slots.isEmpty()) {
                        Toast.makeText(this@BrowseCounselorsActivity, "No slots available.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Create custom dialog
                    val dialog = Dialog(this@BrowseCounselorsActivity)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.dialog_available_slots)

                    // Make background transparent so rounded corners show
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    // Bind views
                    val tvSubtitle = dialog.findViewById<TextView>(R.id.tvDialogSubtitle)
                    val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
                    val rvSlots = dialog.findViewById<RecyclerView>(R.id.rvDialogSlots)

                    // Set header text
                    tvSubtitle.text = "${counselor.firstName} ${counselor.lastName} · ${counselor.specialization}"

                    // Setup RecyclerView
                    rvSlots.layoutManager = LinearLayoutManager(this@BrowseCounselorsActivity)
                    rvSlots.adapter = DialogSlotAdapter(slots) { slotId, formattedDateTime ->
                        dialog.dismiss() // Close popup
                        bookSlot(slotId, "${counselor.firstName} ${counselor.lastName}", formattedDateTime) // Go to next screen
                    }

                    btnClose.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()

                } else {
                    Toast.makeText(this@BrowseCounselorsActivity, "Failed to load slots.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BrowseCounselorsActivity, "Network error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bookSlot(slotId: Long, counselorName: String, dateTimeStr: String) {
        val intent = Intent(this, BookAppointmentActivity::class.java).apply {
            putExtra("SLOT_ID", slotId)
            putExtra("COUNSELOR_NAME", counselorName)
            putExtra("DATE_TIME_STR", dateTimeStr)
        }
        startActivity(intent)
    }
}