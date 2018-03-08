package cn.mw.ethwallet.services

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.network.EtherscanAPI
import cn.mw.ethwallet.utils.Blockies
import cn.mw.ethwallet.utils.ExchangeCalculator
import cn.mw.ethwallet.utils.WalletStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:25
 * @description
 */


class NotificationService : IntentService("Notification Service") {

    override fun onHandleIntent(intent: Intent?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (!prefs.getBoolean("notifications_new_message", true) || WalletStorage.getInstance(this).get()!!.size <= 0) {
            NotificationLauncher.instance.stop()
            return
        }

        try {
            EtherscanAPI.instance.getBalances(WalletStorage.getInstance(this).get()!!, object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    var data: JSONArray? = null
                    try {
                        data = JSONObject(response.body()!!.string()).getJSONArray("result")
                        val preferences = PreferenceManager.getDefaultSharedPreferences(this@NotificationService)

                        var notify = false
                        var amount = BigInteger("0")
                        var address = ""
                        val editor = preferences.edit()
                        for (i in 0 until data!!.length()) {
                            if (preferences.getString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance")) != data.getJSONObject(i).getString("balance")) {
                                if (BigInteger(preferences.getString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance"))).compareTo(BigInteger(data.getJSONObject(i).getString("balance"))) < 1) { // Nur wenn höhere Balance als vorher
                                    notify = true
                                    address = data.getJSONObject(i).getString("account")
                                    amount = amount.add(BigInteger(data.getJSONObject(i).getString("balance")).subtract(BigInteger(preferences.getString(address, "0"))))
                                }
                            }
                            editor.putString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance"))
                        }
                        editor.commit()
                        if (notify) {
                            try {
                                val amountS = BigDecimal(amount).divide(ExchangeCalculator.ONE_ETHER, 4, BigDecimal.ROUND_DOWN).toPlainString()
                                sendNotification(address, amountS)
                            } catch (e: Exception) {

                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun sendNotification(address: String, amount: String) {
        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_ticker))
                .setLights(Color.CYAN, 3000, 3000)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentTitle(this.resources.getString(R.string.notification_title))
                .setAutoCancel(true)
                .setContentText(amount + " ETH")

        if (android.os.Build.VERSION.SDK_INT >= 18)
        // Android bug in 4.2, just disable it for everyone then...
            builder.setVibrate(longArrayOf(1000, 1000))

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val main = Intent(this, MainActivity::class.java)
        main.putExtra("STARTAT", 2)

        val contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(contentIntent)

        val mNotificationId = (Math.random() * 150).toInt()
        mNotifyMgr.notify(mNotificationId, builder.build())
    }
}