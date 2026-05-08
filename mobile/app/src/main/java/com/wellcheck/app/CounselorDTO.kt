package com.wellcheck.app.data

data class CounselorDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val specialization: String,
    val bio: String?,
    val profilePhoto: String?,
    val availableSlots: Int
)