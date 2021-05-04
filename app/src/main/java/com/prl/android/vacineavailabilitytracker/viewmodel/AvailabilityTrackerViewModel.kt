package com.prl.android.vacineavailabilitytracker.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prl.android.vacineavailabilitytracker.TrackerConstants.CENTER_ID
import com.prl.android.vacineavailabilitytracker.TrackerConstants.DATE
import com.prl.android.vacineavailabilitytracker.TrackerConstants.FREQ
import com.prl.android.vacineavailabilitytracker.TrackerConstants.PIN_CODE
import com.prl.android.vacineavailabilitytracker.data.Center
import com.prl.android.vacineavailabilitytracker.data.VaccineAvailability
import com.prl.android.vacineavailabilitytracker.domain.VaccineAvailabilityTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AvailabilityTrackerViewModel : ViewModel() {
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private lateinit var r: Runnable

    fun getData(): LiveData<Center> {
        val data = MutableLiveData<Center>()

        r = Runnable {
            viewModelScope.launch(Dispatchers.IO) {
                Log.d("Pravin", "staring..")
                val response = VaccineAvailabilityTracker.availabilityService.getAvailability(
                    PIN_CODE,
                    DATE
                )
                if (response.isSuccessful) {
                    val body = response.body() as VaccineAvailability
                    val centers = body.centers.forEach { center ->
                        if (center.centerId == CENTER_ID) {
                            data.postValue(center)
                        }
                    }
                }
                handler.postDelayed(r, FREQ)
            }
        }

        handler.postDelayed(r, FREQ)
        return data
    }
}