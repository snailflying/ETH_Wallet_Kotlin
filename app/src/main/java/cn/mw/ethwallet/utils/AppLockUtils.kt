package cn.mw.ethwallet.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v4.app.ActivityCompat

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 13:29
 * @description
 */
object AppLockUtils {

    fun hasDeviceFingerprintSupport(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fingerprintManager = context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                false
            } else fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()
        } else {
            return false
        }
    }

}