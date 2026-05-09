package com.wellcheck.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wellcheck.app.adapter.SlotPickerAdapter
import com.wellcheck.app.network.CounselorListItem
import com.wellcheck.app.network.RetrofitClient
import com.wellcheck.app.network.SlotResponse
import kotlinx.coroutines.launch

class SlotsBottomSheet(
    private val counselor: CounselorListItem,
    private val onBooked: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var slotAdapter: SlotPickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_slots, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle     = view.findViewById<TextView>(R.id.tvSheetTitle)
        val tvSubtitle  = view.findViewById<TextView>(R.id.tvSheetSubtitle)
        val btnClose    = view.findViewById<ImageView>(R.id.btnCloseSheet)
        val pb          = view.findViewById<ProgressBar>(R.id.pbSlots)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmpty)
        val rvSlots     = view.findViewById<RecyclerView>(R.id.rvSlots)

        tvTitle.text    = "Available Slots"
        tvSubtitle.text = "${counselor.firstName} ${counselor.lastName} · ${counselor.specialization ?: ""}"

        btnClose.setOnClickListener { dismiss() }

        slotAdapter = SlotPickerAdapter(requireContext(), emptyList()) { slot ->
            launchBookingFlow(slot)
        }
        rvSlots.layoutManager = LinearLayoutManager(requireContext())
        rvSlots.adapter = slotAdapter

        fetchSlots(pb, layoutEmpty, rvSlots)
    }

    private fun fetchSlots(pb: ProgressBar, layoutEmpty: LinearLayout, rvSlots: RecyclerView) {
        val token = requireContext()
            .getSharedPreferences("wellcheck_prefs", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        pb.visibility          = View.VISIBLE
        rvSlots.visibility     = View.GONE
        layoutEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCounselorSlots("Bearer $token", counselor.id)
                pb.visibility = View.GONE

                if (response.isSuccessful) {
                    val slots = (response.body() ?: emptyList())
                        .filter { it.status == "AVAILABLE" }

                    if (slots.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                    } else {
                        rvSlots.visibility = View.VISIBLE
                        slotAdapter.updateData(slots)
                    }
                } else {
                    layoutEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                pb.visibility          = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun launchBookingFlow(slot: SlotResponse) {
        val intent = Intent(requireContext(), BookAppointmentActivity::class.java).apply {
            putExtra("slot_id",       slot.id)
            putExtra("slot_start",    slot.startTime)
            putExtra("slot_end",      slot.endTime)
            putExtra("counselor_id",  counselor.id)
            putExtra("counselor_name","${counselor.firstName} ${counselor.lastName}")
            putExtra("counselor_spec",counselor.specialization ?: "")
        }
        dismiss()
        startActivity(intent)
    }
}