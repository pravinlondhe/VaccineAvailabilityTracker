package com.prl.android.vaccineavailabilitytracker.domain

import com.prl.android.vaccineavailabilitytracker.data.VaccineAvailability
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VaccineAvailabilityService {

    @POST("/api/v2/auth/generateMobileOTP")
    suspend fun generateOtpForMobile(
        @Query("pincode") pinCode: String,
        @Query("date") date: String
    ): Response<VaccineAvailability>

    @GET("/api/v2/appointment/sessions/public/calendarByPin")
    suspend fun getAvailability(
        @Query("pincode") pinCode: String,
        @Query("date") date: String
    ): Response<VaccineAvailability>

    @GET("/api/v2/appointment/sessions/public/calendarByDistrict")
    suspend fun getAvailabilityByDistrict(
        @Query("district_id") districId: String, // there district_id for pune it is 363
        @Query("date") date: String
    ): Response<VaccineAvailability>

}