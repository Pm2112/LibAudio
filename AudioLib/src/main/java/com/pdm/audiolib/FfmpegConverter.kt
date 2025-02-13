package com.pdm.audiolib

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

object FfmpegConverter {

    fun convertPCMtoMP3(pcmFile: File, mp3File: File) {
        val command = "-f s16le -ar 44100 -ac 1 -i ${pcmFile.absolutePath} -b:a 128k ${mp3File.absolutePath}"

        FFmpegKit.executeAsync(command) { session ->
            val returnCode = session.returnCode
            if (returnCode.isValueSuccess) {
                Log.d("FfmpegConverter", "Chuyển đổi thành công: ${mp3File.absolutePath}")
            } else {
                Log.e("FfmpegConverter", "Chuyển đổi thất bại: ${session.failStackTrace}")
            }
        }
    }
}