package com.pdm.libaudio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pdm.audiolib.AudioRecorder
import com.pdm.libaudio.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val audioRecorder = AudioRecorder(this) { time ->
            binding.timeRecorder.text = time
        }

        var file: File? = null

        binding.btnStart.setOnClickListener {
            audioRecorder.startRecording()
        }

        binding.btnStop.setOnClickListener {
            file = audioRecorder.stopRecording()
        }


    }
}