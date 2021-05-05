package com.prl.android.vaccineavailabilitytracker.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.CENTER_ID
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.FREQ
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.date
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.pin_code
import com.prl.android.vaccineavailabilitytracker.data.Center
import com.prl.android.vaccineavailabilitytracker.data.VaccineAvailability
import com.prl.android.vaccineavailabilitytracker.domain.VaccineAvailabilityTracker
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
                Log.d(TAG, "starting for date:$date")
                val response = VaccineAvailabilityTracker.availabilityService.getAvailability(
                    pin_code,
                    date
                )
                if (response.isSuccessful) {
                    val body = response.body() as VaccineAvailability
                    val centers = body.centers.forEach { center ->
                        if (center.centerId == CENTER_ID) {
                            val sessions = center.sessions
                            sessions.forEach { session ->
                                if (session.availableCapacity > 0) {
                                    data.postValue(center)
                                }
                            }
                        } else {
                            val sessions = center.sessions
                            sessions.forEach { session ->
                                if (session.availableCapacity > 0) {
                                    data.postValue(center)
                                }
                            }
                        }
                    }
                }

                handler.postDelayed(r, FREQ)
            }
        }

        handler.postDelayed(r, FREQ)
        return data
    }

    private companion object {
        const val TAG = "AvailTrackerViewModel"
    }
}