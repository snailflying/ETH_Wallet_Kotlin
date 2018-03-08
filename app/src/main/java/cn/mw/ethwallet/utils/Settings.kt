package cn.mw.ethwallet.utils

import android.content.Context
import android.preference.PreferenceManager

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 13:50
 * @description
 */
object Settings {

    var showTransactionsWithZero = false

    var startWithWalletTab = false

    var walletBeingGenerated = false

    var displayAds = true

    fun initiate(c: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        showTransactionsWithZero = prefs.getBoolean("zeroAmountSwitch", false)
        startWithWalletTab = prefs.getBoolean("startAtWallet", true)
    }

}