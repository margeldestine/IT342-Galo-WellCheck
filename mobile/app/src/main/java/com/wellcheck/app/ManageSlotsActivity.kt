package com.wellcheck.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellcheck.app.databinding.ActivityManageSlotsBinding
import com.wellcheck.app.network.RetrofitClient
import com.wellcheck.app.network.SlotRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ManageSlotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageSlotsBinding
    private var calendar = Calendar.getInstance()
    private val durations = arrayOf("30 minutes", "60 minutes", "90 minutes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageSlotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                updatePreview()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, m)
                calendar.set(Calendar.SECOND, 0)
                updatePreview()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        binding.btnSaveSlot.setOnClickListener {
            saveSlotToBackend()
        }

        binding.rvSlots.layoutManager = LinearLayoutManager(this)
        fetchSlots()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDuration.adapter = adapter
    }

    private fun updatePreview() {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        binding.tvSelectedPreview.text = sdf.format(calendar.time)
    }

    private fun saveSlotToBackend() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val startTime = isoFormat.format(calendar.time)

        val durationMinutes = when(binding.spinnerDuration.selectedItemPosition) {
            0 -> 30
            1 -> 60
            else -> 90
        }

        val endCal = calendar.clone() as Calendar
        endCal.add(Calendar.MINUTE, durationMinutes)
        val endTime = isoFormat.format(endCal.time)

        val request = SlotRequest(startTime, endTime)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.createSlot(token, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@ManageSlotsActivity, "Slot Created Successfully", Toast.LENGTH_SHORT).show()
                    fetchSlots()
                } else {
                    Log.e("API_ERROR", "Error code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Creation failed: ${e.message}")
            }
        }
    }

    private fun fetchSlots() {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMySlots(token)
                if (response.isSuccessful) {
                    val slots = response.body() ?: emptyList()
                    binding.rvSlots.adapter = SlotAdapter(slots) { id ->
                        deleteSlot(id)
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fetch failed: ${e.message}")
            }
        }
    }

    private fun deleteSlot(id: Long) {
        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("accessToken", "")}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteSlot(token, id)
                if (response.isSuccessful) {
                    fetchSlots()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Delete failed")
            }
        }
    }
}