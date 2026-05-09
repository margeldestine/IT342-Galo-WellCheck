package com.wellcheck.app.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

data class LoginRequest(val email: String, val password: String)
data class StudentRegisterRequest(val studentIdNumber: String, val firstName: String, val lastName: String, val program: String, val yearLevel: String, val gender: String, val birthdate: String, val email: String, val password: String)
data class CounselorRegisterRequest(val firstName: String, val lastName: String, val employeeNumber: String, val specialization: String, val bio: String, val email: String, val password: String)
data class CompleteProfileRequest(val studentIdNumber: String, val program: String, val yearLevel: String, val gender: String, val birthdate: String)
data class LoginResponse(val accessToken: String?, val role: String?, val status: String?, val email: String?, val firstName: String?, val lastName: String?, val profilePhoto: String?, val specialization: String?)
data class ApiError(val code: String?, val message: String?, val details: Any?)
data class CompleteCounselorProfileRequest(val employeeNumber: String, val specialization: String, val bio: String)

data class CounselorListItem(val id: Long, val firstName: String?, val lastName: String?, val specialization: String?, val bio: String?, val profilePhoto: String?, val availableSlots: Int, val averageRating: Double, val ratingCount: Int)

data class SlotRequest(
    val startTime: String,
    val endTime: String
)

data class SlotResponse(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val status: String
)

data class DeleteSlotResponse(
    val action: String,
    val message: String
)

data class CounselorProfileResponse(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val employeeNumber: String?,
    val specialization: String?,
    val bio: String?,
    val yearsExperience: Int?,
    val licenseNumber: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val profilePhoto: String?,
    val profilePhotoType: String?,
    val credentialEntries: List<String>,
    val availableDays: List<String>
)

data class CredentialItemRequest(
    @SerializedName("title") val title: String,
    @SerializedName("year")  val year: String
)

data class UpdateCounselorProfileRequest(
    @SerializedName("specialization")  val specialization: String?,
    @SerializedName("bio")             val bio: String?,
    @SerializedName("yearsExperience") val yearsExperience: Int?,
    @SerializedName("licenseNumber")   val licenseNumber: String?,
    @SerializedName("availableDays")   val availableDays: List<String>,
    @SerializedName("credentials")     val credentials: List<CredentialItemRequest>
)

data class AppointmentResponse(
    val id: Long,
    val slotId: Long,
    val startTime: String,
    val endTime: String,
    val counselorFirstName: String?,
    val counselorLastName: String?,
    val counselorSpecialization: String?,
    val counselorProfilePhoto: String?,
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
    val rejectionReason: String?
)

interface ApiService {

    @POST("auth/register/student")
    suspend fun registerStudent(@Body request: StudentRegisterRequest): Response<ResponseBody>

    @POST("auth/register/counselor")
    suspend fun registerCounselor(@Body request: CounselorRegisterRequest): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/complete-profile")
    suspend fun completeProfile(
        @Header("Authorization") token: String,
        @Body request: CompleteProfileRequest
    ): Response<ResponseBody>

    @POST("auth/complete-counselor-profile")
    suspend fun completeCounselorProfile(
        @Header("Authorization") token: String,
        @Body request: CompleteCounselorProfileRequest
    ): Response<ResponseBody>

    @GET("appointments/my")
    suspend fun getMyAppointments(
        @Header("Authorization") token: String
    ): Response<List<AppointmentResponse>>

    @GET("counselors")
    suspend fun getCounselors(
        @Header("Authorization") token: String
    ): Response<List<CounselorListItem>>

    @GET("appointments/counselor")
    suspend fun getCounselorAppointments(
        @Header("Authorization") token: String
    ): Response<List<AppointmentResponse>>

    @GET("slots/my")
    suspend fun getMySlots(
        @Header("Authorization") token: String
    ): Response<List<SlotResponse>>

    @POST("slots")
    suspend fun createSlot(
        @Header("Authorization") token: String,
        @Body request: SlotRequest
    ): Response<ResponseBody>

    @PUT("slots/{id}")
    suspend fun updateSlot(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: SlotRequest
    ): Response<ResponseBody>

    @DELETE("slots/{id}")
    suspend fun deleteSlot(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<DeleteSlotResponse>

    @PUT("appointments/{id}/approve")
    suspend fun approveAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<AppointmentResponse>

    @PUT("appointments/{id}/reject")
    suspend fun rejectAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<AppointmentResponse>

    @GET("auth/profile/counselor")
    suspend fun getMyCounselorProfile(
        @Header("Authorization") token: String
    ): Response<CounselorProfileResponse>

    @PUT("auth/profile/counselor")
    suspend fun updateCounselorProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateCounselorProfileRequest
    ): Response<ResponseBody>
}