package com.wellcheck.app.network

import com.google.gson.annotations.SerializedName
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

data class CounselorResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val specialization: String,

    @SerializedName("bio")
    val description: String?,

    @SerializedName("availableSlots")
    val availableSlotsCount: Int
)

data class AppointmentRequest(
    val slotId: Long,
    val note: String
)

data class AppointmentResponse(
    val id: Long,
    val slotId: Long,
    val startTime: String,
    val endTime: String,
    val counselorFirstName: String?,
    val counselorLastName: String?,
    val counselorSpecialization: String?,
    val studentFirstName: String?,
    val studentLastName: String?,
    val studentIdNumber: String?,
    val studentProgram: String?,
    val studentYearLevel: String?,
    val studentGender: String?,
    val studentBirthdate: String?,
    val studentSchoolIdPhotoUrl: String?,
    val status: String,
    val note: String?,
    val createdAt: String?
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

    @GET("counselors")
    suspend fun getCounselors(@Header("Authorization") token: String): Response<List<CounselorResponse>>

    @GET("slots/counselor/{id}")
    suspend fun getCounselorAvailableSlots(
        @Header("Authorization") token: String,
        @Path("id") counselorId: Long
    ): Response<List<SlotResponse>>

    @POST("appointments") // Make sure this matches your Spring Boot endpoint! (e.g., "appointments" or "api/appointments")
    suspend fun bookAppointment(
        @Header("Authorization") token: String,
        @Body request: AppointmentRequest
    ): Response<Unit>

    @GET("appointments/counselor")
    suspend fun getCounselorAppointments(@Header("Authorization") token: String): retrofit2.Response<List<AppointmentResponse>>

    @PUT("appointments/{id}/approve")
    suspend fun approveAppointment(@Header("Authorization") token: String, @Path("id") id: Long): retrofit2.Response<AppointmentResponse>

    @PUT("appointments/{id}/reject")
    suspend fun rejectAppointment(@Header("Authorization") token: String, @Path("id") id: Long): retrofit2.Response<AppointmentResponse>

    @GET("appointments/my")
    suspend fun getMyAppointments(@Header("Authorization") token: String): retrofit2.Response<List<AppointmentResponse>>


}