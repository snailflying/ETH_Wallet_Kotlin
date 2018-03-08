package cn.mw.ethwallet.activities

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:47
 * @description
 */
class BaseApplication : Application() {

    val isGooglePlayBuild = false

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun track(s: String) {
        return
    }

    fun event(s: String) {
        return
    }

}