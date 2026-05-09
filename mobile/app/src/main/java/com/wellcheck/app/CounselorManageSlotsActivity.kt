package com.wellcheck.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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

        applyFonts()
        setupRecyclerView()
        setupBottomNavigation()

        binding.btnCreateSlot.setOnClickListener {
            showCreateSlotDialog()
        }

        fetchSlots()
    }

    private fun applyFonts() {
        val dmSerifItalic = ResourcesCompat.getFont(this, R.font.dm_serif_display_italic)
        val interBold = ResourcesCompat.getFont(this, R.font.inter_bold)
        val interRegular = ResourcesCompat.getFont(this, R.font.inter_regular)
        binding.tvTitle.typeface = dmSerifItalic
        binding.btnCreateSlot.typeface = interBold
        binding.tvPageLabel.typeface = interBold
        binding.tvSubtitle.typeface = interRegular
    }

    private fun setupRecyclerView() {
        adapter = SlotAdapter(this, emptyList(),
            onEdit = { },
            onDelete = { id -> confirmDelete(id) }
        )
        binding.rvSlots.layoutManager = LinearLayoutManager(this)
        binding.rvSlots.adapter = adapter
    }

    private fun fetchSlots() {
        lifecycleScope.launch {
            try {
                val slotsRes = RetrofitClient.instance.getMySlots("Bearer $token")
                val aptsRes  = RetrofitClient.instance.getCounselorAppointments("Bearer $token")

                if (slotsRes.isSuccessful && aptsRes.isSuccessful) {
                    val slots = slotsRes.body() ?: emptyList()
                    val apts  = aptsRes.body() ?: emptyList()
                    adapter.updateAppointments(apts)           // ← feed appointments first
                    adapter.updateData(slots.sortedBy { it.startTime })
                }
            } catch (e: Exception) {
                Toast.makeText(this@CounselorManageSlotsActivity, "Failed to sync slots", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCreateSlotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_slot, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Rounded corners — must set transparent first so system bg doesn't clip the drawable
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setBackgroundDrawable(
            resources.getDrawable(R.drawable.dialog_rounded_bg, theme)
        )

        // Apply fonts to dialog views
        val dmSerif   = ResourcesCompat.getFont(this, R.font.dm_serif_display_regular)
        val interBold = ResourcesCompat.getFont(this, R.font.inter_bold)
        val interReg  = ResourcesCompat.getFont(this, R.font.inter_regular)

        dialogView.findViewById<TextView>(R.id.tvDialogTitle).typeface    = dmSerif
        dialogView.findViewById<TextView>(R.id.tvDialogSubtitle).typeface = interReg
        dialogView.findViewById<TextView>(R.id.tvLabelDate).typeface      = interBold
        dialogView.findViewById<TextView>(R.id.tvLabelStartTime).typeface = interBold
        dialogView.findViewById<TextView>(R.id.tvLabelDuration).typeface  = interBold
        dialogView.findViewById<TextView>(R.id.tvLabelRepeat).typeface    = interBold
        dialogView.findViewById<TextView>(R.id.tvRepeatNone).typeface     = interBold
        dialogView.findViewById<TextView>(R.id.tvRepeatDaily).typeface    = interReg
        dialogView.findViewById<TextView>(R.id.tvRepeatWeekly).typeface   = interReg

        // Optionally apply to repeat-until label if visible
        dialogView.findViewById<TextView>(R.id.tvLabelRepeatUntil).typeface = interBold

        val btnDate      = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnRepeatDate = dialogView.findViewById<Button>(R.id.btnPickRepeatUntil)
        val layoutRepeat = dialogView.findViewById<View>(R.id.layoutRepeatUntil)
        val tvError      = dialogView.findViewById<TextView>(R.id.tvDialogError)
        val spinnerStart = dialogView.findViewById<Spinner>(R.id.spinnerStartTime)
        val spinnerDur   = dialogView.findViewById<Spinner>(R.id.spinnerDuration)

        val btnNone   = dialogView.findViewById<LinearLayout>(R.id.btnRepeatNone)
        val btnDaily  = dialogView.findViewById<LinearLayout>(R.id.btnRepeatDaily)
        val btnWeekly = dialogView.findViewById<LinearLayout>(R.id.btnRepeatWeekly)

        val tvNone   = dialogView.findViewById<TextView>(R.id.tvRepeatNone)
        val tvDaily  = dialogView.findViewById<TextView>(R.id.tvRepeatDaily)
        val tvWeekly = dialogView.findViewById<TextView>(R.id.tvRepeatWeekly)

        var selectedRepeat = "none"

        fun selectRepeat(type: String) {
            selectedRepeat = type

            btnNone.setBackgroundResource(if (type == "none") R.drawable.repeat_option_selected else R.drawable.repeat_option_unselected)
            btnDaily.setBackgroundResource(if (type == "daily") R.drawable.repeat_option_selected else R.drawable.repeat_option_unselected)
            btnWeekly.setBackgroundResource(if (type == "weekly") R.drawable.repeat_option_selected else R.drawable.repeat_option_unselected)

            tvNone.setTextColor(if (type == "none") 0xFFFFFFFF.toInt() else 0xFF1A1A1A.toInt())
            tvDaily.setTextColor(if (type == "daily") 0xFFFFFFFF.toInt() else 0xFF1A1A1A.toInt())
            tvWeekly.setTextColor(if (type == "weekly") 0xFFFFFFFF.toInt() else 0xFF1A1A1A.toInt())

            // Re-apply fonts after color change (setBackgroundResource can reset typeface on some devices)
            tvNone.typeface   = interBold
            tvDaily.typeface  = if (type == "daily") interBold else interReg
            tvWeekly.typeface = if (type == "weekly") interBold else interReg

            layoutRepeat.visibility = if (type == "none") View.GONE else View.VISIBLE
        }

        selectRepeat("none")

        btnNone.setOnClickListener { selectRepeat("none") }
        btnDaily.setOnClickListener { selectRepeat("daily") }
        btnWeekly.setOnClickListener { selectRepeat("weekly") }

        setupSpinners(spinnerStart, spinnerDur)

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

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogSubmit).setOnClickListener {
            if (validateInputs(selectedDate, selectedRepeat, repeatUntilDate, tvError)) {
                val dates = buildRepeatDates(selectedDate!!, selectedRepeat, repeatUntilDate)
                submitSlots(dates, dialog)
            }
        }

        dialogView.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()

        // Set dialog width after show() so window is not null
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun validateInputs(start: Calendar?, repeat: String, until: Calendar?, errorView: TextView): Boolean {
        if (start == null) {
            errorView.text = "Please select a date."
            errorView.visibility = View.VISIBLE
            return false
        }

        val dayOfWeek = start.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            errorView.text = "Slots can only be created on Monday to Friday."
            errorView.visibility = View.VISIBLE
            return false
        }

        val timeParts = selectedStartTime.split(":")
        val startHour = timeParts[0].toInt()
        val startMin  = timeParts[1].toInt()
        val totalStartMinutes = startHour * 60 + startMin
        val totalEndMinutes   = totalStartMinutes + selectedDuration
        val isMorningOk   = totalStartMinutes >= 480 && totalEndMinutes <= 720
        val isAfternoonOk = totalStartMinutes >= 780 && totalEndMinutes <= 1020

        if (!isMorningOk && !isAfternoonOk) {
            errorView.text = "Time must be within 8:00 AM–12:00 PM or 1:00 PM–5:00 PM."
            errorView.visibility = View.VISIBLE
            return false
        }

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
                    val payload = SlotRequest(
                        startTime = "${dateStr}T${selectedStartTime}:00",
                        endTime   = "${dateStr}T${calculateEndTime(selectedStartTime, selectedDuration)}:00"
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
        sDur.setSelection(1)
        sDur.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedDuration = durations[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun calculateEndTime(startTime: String, duration: Int): String {
        val sdf  = SimpleDateFormat("HH:mm", Locale.US)
        val date = sdf.parse(startTime) ?: return startTime
        val cal  = Calendar.getInstance().apply {
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
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, CounselorProfileActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
    }
}