package com.prl.android.vaccineavailabilitytracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.CENTER_ID
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.FREQ
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.date
import com.prl.android.vaccineavailabilitytracker.TrackerConstants.pin_code
import com.prl.android.vaccineavailabilitytracker.data.Center
import com.prl.android.vaccineavailabilitytracker.data.VaccineAvailability
import com.prl.android.vaccineavailabilitytracker.domain.VaccineAvailabilityTracker
import com.prl.android.vacineavailabilitytracker.R
import kotlinx.coroutines.*
import java.lang.Runnable

private const val NOTIFICATION_ID = 10001
private const val CHANNEL_ID = "10005"

class AvailabilityService : Service() {
    private val serviceScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.IO + Job())
    }
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var r: Runnable

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        Log.d(TAG, "onCreateService")
    }

    private fun createNotificationChannel() {
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)
    }

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setOngoing(true)
            setContentTitle(getString(R.string.app_name))
            setTicker(getString(R.string.app_name))
            setSmallIcon(android.R.drawable.sym_def_app_icon)
            setContentText("Tracking for Date:$date and pinCode:$pin_code")
        }.build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onServiceBind")
        return MyBinder()
    }

    fun starNetworkRequest(desc: AssetFileDescriptor): LiveData<Center> {
        val data = MutableLiveData<Center>()

        r = Runnable {
            serviceScope.launch(Dispatchers.IO) {
                Log.d(TAG, "starting for date:$date")
                val response = VaccineAvailabilityTracker.availabilityService.getAvailability(
                    pin_code,
                    date
                )
                if (response.isSuccessful) {
                    val body = response.body() as VaccineAvailability
                    body.centers.forEach { center ->
                        if (center.centerId == CENTER_ID) {
                            val sessions = center.sessions
//                            playRingtone(desc) // for testing
//                            data.postValue(center) // for testing
                            sessions.forEach { session ->
                                if (session.availableCapacity > 0) {
                                    playRingtone(desc)
                                    data.postValue(center)
                                }
                            }
                        } else {
                            val sessions = center.sessions
                            sessions.forEach { session ->
//                                playRingtone(desc) // for testing
//                                data.postValue(center) // for testing
                                if (session.availableCapacity > 0) {
                                    playRingtone(desc)
                                    data.postValue(center)
                                }
                            }
                        }
                    }
                }
                handler.postDelayed(r, FREQ)
            }
        }

        handler.post(r)
        return data
    }

    fun starNetworkRequestByDistrict(desc: AssetFileDescriptor): LiveData<Center> {
        val data = MutableLiveData<Center>()

        r = Runnable {
            serviceScope.launch(Dispatchers.IO) {
                Log.d(TAG, "starting for date:$date")
                val response =
                    VaccineAvailabilityTracker.availabilityService.getAvailabilityByDistrict(
                        "363",
                        date
                    )
                if (response.isSuccessful) {
                    val body = response.body() as VaccineAvailability
                    body.centers.forEach { center ->
                        if (center.centerId == CENTER_ID) {
                            val sessions = center.sessions
//                            playRingtone(desc) // for testing
//                            data.postValue(center) // for testing
                            sessions.forEach { session ->
                                if (session.availableCapacity > 0) {
                                    playRingtone(desc)
                                    data.postValue(center)
                                }
                            }
                        } else {
                            val sessions = center.sessions
                            sessions.forEach { session ->
//                                playRingtone(desc) // for testing
//                                data.postValue(center) // for testing
                                if (session.availableCapacity > 0) {
                                    playRingtone(desc)
                                    data.postValue(center)
                                }
                            }
                        }
                    }
                }
                handler.postDelayed(r, FREQ)
            }
        }

        handler.post(r)
        return data
    }

    fun stopRingtone() {
        try {
            mediaPlayer.stop()
            mediaPlayer.release()
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Illegal state...")
        }
    }

    private suspend fun playRingtone(desc: AssetFileDescriptor) {
        Log.d(TAG, "Playing music from service...")
        withContext(Dispatchers.Main) {
            mediaPlayer = MediaPlayer()
            mediaPlayer.run {
                if (isPlaying.not()) {
                    setDataSource(desc)
                    setVolume(100f, 100f)
                    prepareAsync()
                    setOnPreparedListener {
                        Log.d(TAG, "Playing music from service")
                        start()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    inner class MyBinder : Binder() {
        val avlService: AvailabilityService = AvailabilityService()
    }

    private companion object {
        const val TAG = "AvailabilityService"
    }
}