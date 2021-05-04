package com.prl.android.vacineavailabilitytracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prl.android.vacineavailabilitytracker.databinding.ActivityMainBinding
import com.prl.android.vacineavailabilitytracker.viewmodel.AvailabilityTrackerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AvailabilityTrackerViewModel
    private lateinit var binding: ActivityMainBinding
    private var availabilityService: AvailabilityService? = null
    private var mediaPlayer: MediaPlayer? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder?) {
            Log.d("Pravin", "onServiceConnected:")
            val b = binder as AvailabilityService.MyBinder
            availabilityService = b.avlService
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d("Pravin", "ServiceDisconnected")
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

        binding.buttonStartTrack.setOnClickListener {
//            viewModelNetworkStartRequest()
            serviceNetworkRequest()
        }

        binding.buttonStopRing.setOnClickListener {
            stopRingtone()
            availabilityService?.stopRingtone()
        }
    }

    private fun playRingtone() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
        }
        mediaPlayer?.run {
//            prepareAsync()
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
        availabilityService?.starNetworkRequest()?.observe(this) {
            it?.let {
                val text = "${it.name}-${System.currentTimeMillis()}: ${it.sessions}"
                binding.tvOutput.text = text
                playRingtone()
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
}