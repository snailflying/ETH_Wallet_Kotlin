package cn.mw.ethwallet.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:26
 * @description
 */
object ExternalStorageHandler {

    val REQUEST_WRITE_STORAGE = 112
    val REQUEST_READ_STORAGE = 113

    val isExternalStorageReadOnly: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState) {
                true
            } else false
        }

    val isExternalStorageAvailable: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == extStorageState) {
                true
            } else false
        }

    fun askForPermission(c: Activity) {
        if (Build.VERSION.SDK_INT < 23) return
        ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
    }

    fun askForPermissionRead(c: Activity) {
        if (Build.VERSION.SDK_INT < 23) return
        ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_STORAGE)
    }

    fun hasReadPermission(c: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    fun hasPermission(c: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        } else {
            return true
        }
        return false
    }
}