package com.pdm.audiolib

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.activity.ComponentActivity
import java.io.File

class AudioRecorder(private val activity: ComponentActivity) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val audioData = mutableListOf<Byte>()

    private val permissionHandler = PermissionHandler(activity)
    private val audioSaver = AudioSaver()

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 44100
    }

    /**
     * Bắt đầu ghi âm (xin quyền nếu chưa cấp)
     */
    fun startRecording() {
        if (isRecording) {
            Log.w(TAG, "Đã bắt đầu ghi âm, không thể ghi lại.")
            return
        }

        permissionHandler.checkAudioPermission {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            )

            audioRecord?.startRecording()
            isRecording = true

            Thread {
                val buffer = ByteArray(1024)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        synchronized(audioData) {
                            audioData.addAll(buffer.take(read))
                        }
                    }
                }
            }.start()

            Log.d(TAG, "Đã bắt đầu ghi âm.")
        }
    }

    /**
     * Dừng ghi âm
     */
    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        Log.d(TAG, "Đã dừng ghi âm.")
    }

    /**
     * Lưu file ghi âm với nhiều định dạng (PCM, WAV, MP3)
     */
    fun saveRecording(file: File, format: AudioFormatType) {
        audioSaver.saveAudio(file, format, audioData)
    }

    /**
     * Hủy dữ liệu ghi âm tạm thời (không lưu)
     */
    fun discardRecording() {
        synchronized(audioData) { audioData.clear() }
        Log.d(TAG, "Đã hủy dữ liệu ghi âm.")
    }

    fun isRecording(): Boolean = isRecording
}
