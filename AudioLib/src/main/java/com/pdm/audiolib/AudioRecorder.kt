package com.pdm.audiolib

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import java.io.File
import java.util.Locale

class AudioRecorder(
    private val activity: ComponentActivity,
    private val onTimeUpdate: (String) -> Unit = {}
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val audioData = mutableListOf<Byte>()
    private var recordingStartTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val permissionHandler = PermissionHandler(activity)
    private val audioSaver = AudioSaver(activity)

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024
        private const val MILLI_SECOND = 1000L
        private const val MINUTE_UNIT = 60
    }

    /**
     * Bắt đầu ghi âm
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
            recordingStartTime = System.currentTimeMillis()

            Thread {
                val buffer = ByteArray(BUFFER_SIZE)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        synchronized(audioData) {
                            audioData.addAll(buffer.take(read))
                        }
                    }
                    updateRecordingTime()
                }
            }.start()

            Log.d(TAG, "Đã bắt đầu ghi âm.")
        }
    }

    /**
     * Dừng ghi âm và lưu file MP3 tạm thời
     */
    fun stopRecording(): File? {
        if (!isRecording) return null

        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null

        handler.removeCallbacksAndMessages(null) // Dừng cập nhật UI
        updateRecordingTime(final = true)

        return audioSaver.saveTemporaryAudio(audioData)
    }

    /**
     * Xác nhận lưu file chính thức, xóa file tạm
     */
    fun finalizeRecording(finalFileName: String): File {
        return audioSaver.finalizeRecording(finalFileName)
    }

    /**
     * Hủy file ghi âm tạm thời
     */
    fun discardRecording() {
        audioSaver.deleteTemporaryAudio()
    }

    /**
     * Lấy file MP3 tạm thời
     */
    fun getTemporaryMp3File(): File {
        return audioSaver.getTemporaryFile()
    }

    /**
     * Lấy đường dẫn file MP3 tạm thời
     */
    fun getTemporaryMp3FilePath(): String {
        return audioSaver.getTemporaryFilePath()
    }

    /**
     * Cập nhật thời gian ghi âm lên UI
     */
    private fun updateRecordingTime(final: Boolean = false) {
        if (!isRecording && !final) return

        val elapsedTime = System.currentTimeMillis() - recordingStartTime
        val seconds = (elapsedTime / MILLI_SECOND) % MINUTE_UNIT
        val minutes = (elapsedTime / MILLI_SECOND) / MINUTE_UNIT

        val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

        handler.post {
            onTimeUpdate(formattedTime)
        }

        if (!final) {
            handler.postDelayed({ updateRecordingTime() }, MILLI_SECOND)
        }
    }

    fun isRecording(): Boolean = isRecording
}