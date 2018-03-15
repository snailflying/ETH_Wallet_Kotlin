package cn.mw.ethwallet.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import cn.mw.ethwallet.utils.WalletStorage

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:06
 * @description
 */
class NotificationLauncher private constructor() {

    private var pintent: PendingIntent? = null
    private var service: AlarmManager? = null

    fun start(c: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        if (prefs.getBoolean("notifications_new_message", true) && WalletStorage.getInstance(c).get()!!.size >= 1) {
            service = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(c, NotificationService::class.java)
            pintent = PendingIntent.getService(c, 23, i, 0)

            val syncInt = Integer.parseInt(prefs.getString("sync_frequency", "4"))

            service!!.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR * syncInt, pintent)
        }
    }

    fun stop() {
        if (service == null || pintent == null) return
        service!!.cancel(pintent)
    }

    companion object {
        val instance: NotificationLauncher by lazy { NotificationLauncher() }

//        fun getINSTANCE(): NotificationLauncher {
//            if (INSTANCE == null)
//                INSTANCE = NotificationLauncher()
//            return INSTANCE
//        }
    }

}