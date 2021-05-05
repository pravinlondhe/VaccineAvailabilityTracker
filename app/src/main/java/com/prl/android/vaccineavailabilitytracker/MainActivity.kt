package com.prl.android.vaccineavailabilitytracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prl.android.vaccineavailabilitytracker.viewmodel.AvailabilityTrackerViewModel
import com.prl.android.vacineavailabilitytracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AvailabilityTrackerViewModel
    private lateinit var binding: ActivityMainBinding
    private var availabilityService: AvailabilityService? = null
    private var mediaPlayer: MediaPlayer? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder?) {
            Log.d(TAG, "onServiceConnected:")
            val b = binder as AvailabilityService.MyBinder
            availabilityService = b.avlService
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d(TAG, "ServiceDisconnected")
            availabilityService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel =
            ViewModelProvider.NewInstanceFactory().create(AvailabilityTrackerViewModel::class.java)
        val intent = Intent(this, AvailabilityService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        with(binding) {
            etPinCode.setText(TrackerConstants.pin_code)
            tvOutput.movementMethod = ScrollingMovementMethod()
            buttonStartTrack.setOnClickListener {
//            viewModelNetworkStartRequest()
                if (etPinCode.text.isNotBlank()) {
                    TrackerConstants.pin_code = etPinCode.text.toString()
                }
                serviceNetworkRequest()
            }

            buttonStopRing.setOnClickListener {
                stopRingtone()
                availabilityService?.stopRingtone()
            }
        }
    }

    private fun playRingtone() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
        }
        mediaPlayer?.run {
            setOnPreparedListener {
                start()
                binding.buttonStopRing.visibility = View.VISIBLE
            }
        }
    }

    private fun stopRingtone(){
        mediaPlayer?.stop()
    }

    private fun serviceNetworkRequest() {
        availabilityService?.starNetworkRequest(assets.openFd(FILE_NAME))?.observe(this) {
            it?.let {
                val tvText = "${binding.tvOutput.text} \n"
                val text = "$tvText \n ${it.name}-${System.currentTimeMillis()}: ${it.sessions}"
                binding.tvOutput.text = text
                binding.buttonStopRing.visibility = View.VISIBLE
            }
        }
    }

    private fun viewModelNetworkStartRequest() {
        viewModel.getData().observe(this) {
            it?.let {
                val text = "${it.name}-${System.currentTimeMillis()}: ${it.sessions}"
                binding.tvOutput.text = text
                playRingtone()
            }
        }
    }

    private companion object {
        const val TAG = "MainActivity"
        const val FILE_NAME = "siren.mp3"
    }
}