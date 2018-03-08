package cn.mw.ethwallet.interfaces

import android.hardware.fingerprint.FingerprintManager

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 12:01
 * @description
 */
interface FingerprintListener {
    fun authenticationFailed(error: String)

    fun authenticationSucceeded(result: FingerprintManager.AuthenticationResult)
}