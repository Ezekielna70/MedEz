package com.protel.myapplication.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class patSignupRequest(
    val pat_username: String,
    val pat_email: String,
    val pat_password: String,
    val pat_age: Int,
    val dev_id: String
)
data class careSignupRequest(
    val care_username: String,
    val care_email: String,
    val care_password: String,
    val care_age: Int
)

data class SignupResponse(
    val status: String,
    val message: String,
    val error: String? = null
)

data class patLoginRequest(
    val pat_email: String,
    val pat_password: String
)

data class careLoginRequest(
    val care_email: String,
    val care_password: String
)

data class patLoginResponse(
    val status: String, // Status login, misalnya "success" atau "error"
    val message: String, // Pesan sukses atau error
    val data: patData? // Objek data user
)

data class patData(
    val pat_id: String, // ID pasien
    val pat_username: String, // Username pasien
    val pat_email: String, // Email pasien
    val pat_age: Int // Usia pasien
)

data class careLoginResponse(
    val status: String, // Status login, misalnya "success" atau "error"
    val message: String, // Pesan sukses atau error
    val data: careData? // Objek data user
)

data class careData(
    val care_id: String, // ID pasien
    val care_username: String, // Username pasien
    val care_email: String, // Email pasien
    val care_age: Int // Usia pasien
)

data class Reminder(
    val med_id : String,
    val med_username: String,          // Corresponds to "med_username"
    val med_dosage: Int,            // Corresponds to "med_dosage"
    val med_function: String,          // Corresponds to "med_function"
    var med_remaining: Int,            // Corresponds to "med_remaining"
    val consumption_times: List<String>, // Corresponds to "consumption_times"
    val med_slot: Int
)

data class PatientsResponse(
    val patients: List<Patient>,
    val status: String
)

data class Patient(
    val DevID: String,
    val PatAge: Int,
    val PatEmail: String,
    val PatID: String,
    val PatUsername: String
)

data class DeviceStoreRequest(
    val dev_id: String,
    val dev_username: String,
    val dev_status: String,
    val dev_time: String,
    val care_id: String
)

data class DeviceStoreResponse(
    val device: Device?,
    val message: String?,
    val details: String?,
    val error: String?
)

data class Device(
    val care_id: String,
    val dev_id: String,
    val dev_status: String,
    val dev_time: String,
    val dev_username: String
)

data class Medicine(
    val med_id: String,
    val med_status: String,
    val med_username: String,
    val med_dosage: Int,
    val med_function: String,
    val med_remaining: Int,
    val consumption_times: List<String>,
    val med_slot : Int
)

data class MedicinesResponse(
    val medicines: List<Medicine>?,
    val status: String
)

data class DeleteMedicineResponse(
    val message: String,
    val status: String
)

data class AddMedicineRequest(
    val pat_id: String,
    val medicine: MedicineRequest
)

data class MedicineRequest(
    val med_username: String,
    val med_dosage: Int,
    val med_function: String,
    val med_remaining: Int,
    val consumption_times: List<String>,
    val med_slot: Int
)

data class AddMedicineResponse(
    val med_id: String,
    val message: String,
    val status: String
)

data class DecreaseMedRequest(
    val dev_id: String,
    val med_id: String,
    val med_remaining: Int
)

data class DecreaseMedResponse(
    val message: String,
    val status: String
)

interface ApiService {
    @POST("patient/signup")
    fun patSignUp(@Body request: patSignupRequest): Call<SignupResponse>

    @POST("caregiver/signup")
    fun careSignUp(@Body request: careSignupRequest): Call<SignupResponse>

    @POST("patient/login")
    fun patLogin(@Body request: patLoginRequest): Call<patLoginResponse>

    @POST("caregiver/login")
    fun careLogin(@Body request: careLoginRequest): Call<careLoginResponse>

    @GET("caregiver/getall/{caregiver_id}")
    fun getPatients(@Path("caregiver_id") caregiverId: String): Call<PatientsResponse>

    @POST("device/store")
    fun storeDevice(@Body request: DeviceStoreRequest): Call<DeviceStoreResponse>

    @GET("caregiver/getmed/{patient_id}")
    fun getMedicines(@Path("patient_id") patientId: String): Call<MedicinesResponse>

    @DELETE("caregiver/deletemed/{pat_id}/{med_id}")
    fun deleteMedicine(
        @Path("pat_id") patientId: String,
        @Path("med_id") medicineId: String
    ): Call<DeleteMedicineResponse>

    @POST("caregiver/addmed")
    fun addMedicine(@Body request: AddMedicineRequest): Call<AddMedicineResponse>

    @POST("decreasemed")
    fun decreaseMed(@Body request: DecreaseMedRequest): Call<DecreaseMedResponse>
}
