package cn.mw.ethwallet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.mw.ethwallet.services.NotificationLauncher

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 22:26
 * @description
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationLauncher.instance.start(context)
    }

}