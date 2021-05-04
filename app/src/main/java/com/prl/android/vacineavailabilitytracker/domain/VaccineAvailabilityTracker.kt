package com.prl.android.vacineavailabilitytracker.domain

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object VaccineAvailabilityTracker {
    private const val BASE_URL = "https://cdn-api.co-vin.in/"
    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().apply {
            addConverterFactory(GsonConverterFactory.create())
            baseUrl(BASE_URL)
            client(okHttpClient)
        }.build()
    }

    val availabilityService = retrofit.create(VaccineAvailabilityService::class.java)

    fun getAvailabilityServices() : VaccineAvailabilityService{
        Log.d("Pravin","sending request")
        val client = OkHttpClient.Builder().apply{
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }.build()
        val retrofit = Retrofit.Builder().apply {
            client(client)
            baseUrl(BASE_URL)
            addConverterFactory(GsonConverterFactory.create())
        }.build()

        return retrofit.create(VaccineAvailabilityService::class.java)
    }

}