package com.prl.android.vacineavailabilitytracker.domain

import com.prl.android.vacineavailabilitytracker.data.VaccineAvailability
import retrofit2.Response
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Query

interface VaccineAvailabilityService {

    @GET("/api/v2/appointment/sessions/public/calendarByPin")
    suspend fun getAvailability(
        @Query("pincode") pinCode: String,
        @Query("date") date: String
    ): Response<VaccineAvailability>
}