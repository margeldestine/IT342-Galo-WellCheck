package com.wellcheck.app.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// --- Registration Requests ---

data class StudentRegisterRequest(
    val studentIdNumber: String,
    val firstName: String,
    val lastName: String,
    val program: String,
    val yearLevel: String,
    val gender: String,
    val birthdate: String,
    val email: String,
    val password: String
)

data class CounselorRegisterRequest(
    val firstName: String,
    val lastName: String,
    val employeeNumber: String,
    val specialization: String,
    val bio: String,
    val email: String,
    val password: String
)

// --- Login & Slots ---

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String?,
    val role: String?,
    val status: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?
)

data class SlotRequest(
    val startTime: String,
    val endTime: String
)

data class SlotResponse(
    val id: Long,
    val counselorId: Long,
    val counselorFirstName: String,
    val counselorLastName: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val createdAt: String
)

// --- Endpoints ---

interface ApiService {

    @POST("auth/register/student")
    suspend fun registerStudent(@Body request: StudentRegisterRequest): Response<ResponseBody>

    @POST("auth/register/counselor")
    suspend fun registerCounselor(@Body request: CounselorRegisterRequest): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("slots/my")
    suspend fun getMySlots(@Header("Authorization") token: String): Response<List<SlotResponse>>

    @POST("slots")
    suspend fun createSlot(
        @Header("Authorization") token: String,
        @Body request: SlotRequest
    ): Response<SlotResponse>

    @DELETE("slots/{id}")
    suspend fun deleteSlot(
        @Header("Authorization") token: String,
        @Path("id") slotId: Long
    ): Response<Unit>
}