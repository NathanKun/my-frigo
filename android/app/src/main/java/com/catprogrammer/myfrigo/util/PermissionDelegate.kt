package com.catprogrammer.myfrigo.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

internal class PermissionsDelegate(private val activity: Activity) {

    fun hasCameraPermission(): Boolean {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        )
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(): Boolean {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
        )
    }

    fun resultGranted(requestCode: Int,
                      permissions: Array<String>,
                      grantResults: IntArray): Boolean {

        if (requestCode != REQUEST_CODE) {
            return false
        }

        if (grantResults.size < 2) {
            return false
        }

        if (!permissions.contains(Manifest.permission.CAMERA)
                || !permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return false
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        requestPermissions()
        return false
    }

    companion object {

        private val REQUEST_CODE = 10
    }
}