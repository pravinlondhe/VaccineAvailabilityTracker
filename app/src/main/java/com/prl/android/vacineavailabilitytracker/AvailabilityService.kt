package com.prl.android.vacineavailabilitytracker

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.prl.android.vacineavailabilitytracker.TrackerConstants.CENTER_ID
import com.prl.android.vacineavailabilitytracker.TrackerConstants.DATE
import com.prl.android.vacineavailabilitytracker.TrackerConstants.FREQ
import com.prl.android.vacineavailabilitytracker.TrackerConstants.PIN_CODE
import com.prl.android.vacineavailabilitytracker.data.Center
import com.prl.android.vacineavailabilitytracker.data.VaccineAvailability
import com.prl.android.vacineavailabilitytracker.domain.VaccineAvailabilityTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AvailabilityService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private lateinit var r: Runnable

    override fun onCreate() {
        super.onCreate()
        Log.d("Pravin", "onCreateService")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.siren)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("Pravin", "onServiceBind")
        return MyBinder()
    }

    fun starNetworkRequest(): LiveData<Center> {
        val data = MutableLiveData<Center>()

        r = Runnable {
            GlobalScope.launch(Dispatchers.IO) {
                Log.d("Pravin", "staring..")
                val response = VaccineAvailabilityTracker.availabilityService.getAvailability(
                    PIN_CODE,
                    DATE
                )
                if (response.isSuccessful) {
                    val body = response.body() as VaccineAvailability
                    val centers = body.centers.forEach { center ->
                        if (center.centerId == CENTER_ID) {
                            playRingtone()
                            data.postValue(center)
                            val sessions = center.sessions
                            sessions.forEach { session ->
                                if (session.availableCapacity > 0) {
                                    withContext(Dispatchers.Main) {
                                        playRingtone()
                                        data.postValue(center)
                                    }
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

    fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
    }

    private fun playRingtone() {
        mediaPlayer?.run {
//            prepareAsync()
            prepare()
            setOnPreparedListener {
                start()
            }
        }
    }

    inner class MyBinder : Binder() {
        val avlService: AvailabilityService = AvailabilityService()
    }
}