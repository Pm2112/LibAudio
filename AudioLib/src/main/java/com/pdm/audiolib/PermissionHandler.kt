package com.pdm.audiolib

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class PermissionHandler(private val activity: ComponentActivity) {
    private val permissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onPermissionResult?.invoke(isGranted)
        }

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    fun checkAudioPermission(onGranted: () -> Unit) {
        if (isAudioPermissionGranted()) {
            onGranted()
        } else {
            requestAudioPermission { isGranted ->
                if (isGranted) {
                    Log.d("PermissionHandler", "Quyền ghi âm đã được cấp.")
                    onGranted()
                } else {
                    Log.w("PermissionHandler", "Quyền ghi âm bị từ chối.")
                }
            }
        }
    }

    private fun isAudioPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission(onPermissionResult: (Boolean) -> Unit) {
        this.onPermissionResult = onPermissionResult
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
