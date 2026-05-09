package com.wellcheck.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellcheck.app.adapter.SlotAdapter
import com.wellcheck.app.databinding.ActivityCounselorManageSlotsBinding
import com.wellcheck.app.network.RetrofitClient
import com.wellcheck.app.network.SlotRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CounselorManageSlotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselorManageSlotsBinding
    private lateinit var adapter: SlotAdapter
    private var token: String = ""

    // State for creating slots
    private var selectedDate: Calendar? = null
    private var repeatUntilDate: Calendar? = null
    private var selectedStartTime: String = "08:00"
    private var selectedDuration: Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselorManageSlotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("wellcheck_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        setupRecyclerView()
        setupBottomNavigation()

        binding.btnCreateSlot.setOnClickListener {
            showCreateSlotDialog()
        }

        fetchSlots()
    }

    private fun setupRecyclerView() {
        adapter = SlotAdapter(this, emptyList(),
            onEdit = { /* Inline edit logic */ },
            onDelete = { id -> confirmDelete(id) }
        )
        binding.rvSlots.layoutManager = LinearLayoutManager(this)
        binding.rvSlots.adapter = adapter
    }

    private fun fetchSlots() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMySlots("Bearer $token")
                if (response.isSuccessful) {
                    val slots = response.body() ?: emptyList()


                    adapter.updateData(slots.sortedBy { it.startTime })

                }
            } catch (e: Exception) {
                Toast.makeText(this@CounselorManageSlotsActivity, "Failed to sync slots", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCreateSlotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_slot, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val btnDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnRepeatDate = dialogView.findViewById<Button>(R.id.btnPickRepeatUntil)
        val layoutRepeat = dialogView.findViewById<View>(R.id.layoutRepeatUntil)
        val rgRepeat = dialogView.findViewById<RadioGroup>(R.id.rgRepeat)
        val tvError = dialogView.findViewById<TextView>(R.id.tvDialogError)
        val spinnerStart = dialogView.findViewById<Spinner>(R.id.spinnerStartTime)
        val spinnerDur = dialogView.findViewById<Spinner>(R.id.spinnerDuration)

        setupSpinners(spinnerStart, spinnerDur)

        rgRepeat.setOnCheckedChangeListener { _, checkedId ->
            layoutRepeat.visibility = if (checkedId == R.id.rbNone) View.GONE else View.VISIBLE
        }

        btnDate.setOnClickListener {
            showDatePicker { cal ->
                selectedDate = cal
                btnDate.text = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(cal.time)
                tvError.visibility = View.GONE
            }
        }

        btnRepeatDate.setOnClickListener {
            showDatePicker { cal ->
                repeatUntilDate = cal
                btnRepeatDate.text = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(cal.time)
                tvError.visibility = View.GONE
            }
        }

        dialogView.findViewById<Button>(R.id.btnDialogSubmit).setOnClickListener {
            val repeatType = when (rgRepeat.checkedRadioButtonId) {
                R.id.rbDaily -> "daily"
                R.id.rbWeekly -> "weekly"
                else -> "none"
            }

            if (validateInputs(selectedDate, repeatType, repeatUntilDate, tvError)) {
                val dates = buildRepeatDates(selectedDate!!, repeatType, repeatUntilDate)
                submitSlots(dates, dialog)
            }
        }

        dialogView.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun validateInputs(start: Calendar?, repeat: String, until: Calendar?, errorView: TextView): Boolean {
        if (start == null) {
            errorView.text = "Please select a date."
            errorView.visibility = View.VISIBLE
            return false
        }

        // 1. Weekday Check
        val dayOfWeek = start.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            errorView.text = "Slots can only be created on Monday to Friday."
            errorView.visibility = View.VISIBLE
            return false
        }

        // 2. Time Block Check (Fixed to ignore hidden system time)
        val timeParts = selectedStartTime.split(":")
        val startHour = timeParts[0].toInt()
        val startMin = timeParts[1].toInt()

        val totalStartMinutes = startHour * 60 + startMin
        val totalEndMinutes = totalStartMinutes + selectedDuration

        val isMorningOk = totalStartMinutes >= 480 && totalEndMinutes <= 720   // 8:00 AM - 12:00 PM
        val isAfternoonOk = totalStartMinutes >= 780 && totalEndMinutes <= 1020 // 1:00 PM - 5:00 PM

        if (!isMorningOk && !isAfternoonOk) {
            errorView.text = "Time must be within 8:00 AM–12:00 PM or 1:00 PM–5:00 PM."
            errorView.visibility = View.VISIBLE
            return false
        }

        // 3. Repeat Check
        if (repeat != "none") {
            if (until == null) {
                errorView.text = "Please choose an end date for the repeat."
                errorView.visibility = View.VISIBLE
                return false
            }
            if (!until.after(start)) {
                errorView.text = "Repeat end date must be after the start date."
                errorView.visibility = View.VISIBLE
                return false
            }
        }
        return true
    }

    private fun buildRepeatDates(start: Calendar, repeat: String, until: Calendar?): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dates = mutableListOf(sdf.format(start.time))
        if (repeat == "none" || until == null) return dates

        val increment = if (repeat == "daily") 1 else 7
        val current = start.clone() as Calendar
        while (true) {
            current.add(Calendar.DATE, increment)
            if (current.after(until)) break
            val day = current.get(Calendar.DAY_OF_WEEK)
            if (repeat == "daily" && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) continue
            dates.add(sdf.format(current.time))
        }
        return dates
    }

    private fun submitSlots(dates: List<String>, dialog: AlertDialog) {
        lifecycleScope.launch {
            try {
                dates.forEach { dateStr ->
                    // Corrected Constructor call
                    val payload = SlotRequest(
                        startTime = "${dateStr}T${selectedStartTime}:00",
                        endTime = "${dateStr}T${calculateEndTime(selectedStartTime, selectedDuration)}:00"
                    )
                    RetrofitClient.instance.createSlot("Bearer $token", payload)
                }
                fetchSlots()
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(this@CounselorManageSlotsActivity, "Failed to create", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners(sTime: Spinner, sDur: Spinner) {
        val times = listOf("08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30")
        sTime.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, times)
        sTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedStartTime = times[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val durations = listOf(15, 30, 45, 60)
        sDur.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durations.map { "$it min" })
        sDur.setSelection(1) // Default to 30 min
        sDur.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { selectedDuration = durations[p2] }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun calculateEndTime(startTime: String, duration: Int): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        val date = sdf.parse(startTime) ?: return startTime
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, duration)
        }
        return sdf.format(cal.time)
    }

    private fun showDatePicker(onSelected: (Calendar) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            onSelected(Calendar.getInstance().apply { set(y, m, d) })
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun confirmDelete(id: Long) {
        AlertDialog.Builder(this)
            .setTitle("Delete Slot")
            .setMessage("Are you sure you want to delete this available time?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    RetrofitClient.instance.deleteSlot("Bearer $token", id)
                    fetchSlots()
                }
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            startActivity(Intent(this, CounselorDashboardActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        binding.navRequests.setOnClickListener {
            startActivity(Intent(this, CounselorRequestsActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

    }
}