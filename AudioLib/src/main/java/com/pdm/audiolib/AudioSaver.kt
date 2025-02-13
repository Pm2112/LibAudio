package com.pdm.audiolib

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioSaver(private val context: Context) {

    companion object {
        private const val TEMP_FILE_NAME = "temp_recording"
    }

    /**
     * Lưu file MP3 tạm thời
     */
    fun saveTemporaryAudio(audioData: List<Byte>): File {
        val pcmTempFile = getInternalFile("temp_audio.pcm")
        val mp3File = getTemporaryFile()

        saveAsPCM(pcmTempFile, audioData)
        FfmpegConverter.convertPCMtoMP3(pcmTempFile, mp3File)
        pcmTempFile.delete()

        return mp3File
    }

    /**
     * Xác nhận lưu file chính thức, xóa file tạm
     */
    fun finalizeRecording(finalFileName: String): File {
        val tempFile = getTemporaryFile()
        val finalFile = getInternalFile(finalFileName)

        if (tempFile.exists()) {
            tempFile.renameTo(finalFile)
            Log.d("AudioSaver", "File ghi âm đã lưu: ${finalFile.absolutePath}")
            tempFile.delete()
        }

        return finalFile
    }

    /**
     * Hủy file ghi âm tạm thời
     */
    fun deleteTemporaryAudio() {
        val tempFile = getTemporaryFile()
        if (tempFile.exists()) {
            tempFile.delete()
            Log.d("AudioSaver", "Đã xóa file ghi âm tạm thời.")
        }
    }

    /**
     * Lấy file MP3 tạm thời
     */
    fun getTemporaryFile(): File {
        return getInternalFile(TEMP_FILE_NAME)
    }

    private fun saveAsPCM(file: File, audioData: List<Byte>) {
        try {
            FileOutputStream(file).use { it.write(audioData.toByteArray()) }
            Log.d("AudioSaver", "Đã lưu file PCM tạm tại: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("AudioSaver", "Lỗi khi lưu file PCM", e)
        }
    }

    private fun getInternalFile(fileName: String): File {
        return File(context.filesDir, "$fileName.mp3")
    }
}