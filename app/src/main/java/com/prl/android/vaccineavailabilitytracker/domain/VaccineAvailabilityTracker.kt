package com.prl.android.vaccineavailabilitytracker.domain

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object VaccineAvailabilityTracker {
    private const val TAG = "VaccineAvaTracker"
    private const val USER_AGENT = "User-Agent"
    private const val CHROME_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36"
    private const val BASE_URL = "https://cdn-api.co-vin.in/"
    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor(UserAgentInterceptor())
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

    fun getAvailabilityServices(): VaccineAvailabilityService {
        Log.d(TAG, "sending request")
        val client = OkHttpClient.Builder().apply {
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

    class UserAgentInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val modifiedRequest = originalRequest.newBuilder().apply {
                header(USER_AGENT, CHROME_USER_AGENT)
            }.build()
            return chain.proceed(modifiedRequest)
        }

    }
}