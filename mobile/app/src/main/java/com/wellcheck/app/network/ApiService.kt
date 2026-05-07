package com.wellcheck.app.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ── Request bodies ──
data class LoginRequest(
    val email: String,
    val password: String
)

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

data class CompleteProfileRequest(
    val studentIdNumber: String,
    val program: String,
    val yearLevel: String,
    val gender: String,
    val birthdate: String
)

// ── Responses ──
data class LoginResponse(
    val accessToken: String?,
    val role: String?,
    val status: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val profilePhoto: String?,
    val specialization: String?
)

data class ApiError(
    val code: String?,
    val message: String?,
    val details: Any?
)

// ── Endpoints ──
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
}