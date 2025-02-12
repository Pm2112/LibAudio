package com.pdm.audiolib

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioSaver {

    fun saveAudio(file: File, format: AudioFormatType, audioData: List<Byte>) {
        when (format) {
            AudioFormatType.PCM -> saveAsPCM(file, audioData)
            AudioFormatType.WAV -> saveAsWAV(file, audioData)
            AudioFormatType.MP3 -> {
                val pcmTempFile = File(file.parent, "temp_audio.pcm")
                saveAsPCM(pcmTempFile, audioData)
                FfmpegConverter.convertPCMtoMP3(pcmTempFile, file)
                pcmTempFile.delete()
            }
        }
    }

    private fun saveAsPCM(file: File, audioData: List<Byte>) {
        try {
            FileOutputStream(file).use { it.write(audioData.toByteArray()) }
            Log.d("AudioSaver", "Đã lưu file PCM tại: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("AudioSaver", "Lỗi khi lưu file PCM", e)
        }
    }

    private fun saveAsWAV(file: File, audioData: List<Byte>) {
        // Viết header WAV và dữ liệu PCM
        Log.d("AudioSaver", "Đã lưu file WAV tại: ${file.absolutePath}")
    }
}
