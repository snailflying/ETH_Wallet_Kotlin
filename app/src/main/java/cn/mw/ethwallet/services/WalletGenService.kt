package cn.mw.ethwallet.services

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.domain.request.FullWallet
import cn.mw.ethwallet.utils.*
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.CipherException
import org.web3j.crypto.ECKeyPair
import java.io.File
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:22
 * @description
 */


class WalletGenService : IntentService("WalletGen Service") {

    private var builder: NotificationCompat.Builder? = null
    internal val mNotificationId = 152

    private var normalMode = true

    override fun onHandleIntent(intent: Intent?) {
        val password = intent!!.getStringExtra("PASSWORD")
        var privatekey = ""

        if (intent.hasExtra("PRIVATE_KEY")) {
            normalMode = false
            privatekey = intent.getStringExtra("PRIVATE_KEY")
        }

        sendNotification()
        try {
            val walletAddress: String
            if (normalMode) { // Create new key
                walletAddress = OwnWalletUtils.generateNewWalletFile(password, File(this.filesDir, ""), true)
            } else { // Privatekey passed
                val keys = ECKeyPair.create(Hex.decode(privatekey))
                walletAddress = OwnWalletUtils.generateWalletFile(password, keys, File(this.filesDir, ""), true)
            }

            WalletStorage.getInstance(this).add(FullWallet("0x" + walletAddress, walletAddress), this)
            AddressNameConverter.getInstance(this).put("0x" + walletAddress, "Wallet " + ("0x" + walletAddress).substring(0, 6), this)
            Settings.walletBeingGenerated = false

            finished("0x" + walletAddress)
        } catch (e: CipherException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }

    }

    private fun sendNotification() {
        builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x2d435c)
                .setTicker(if (normalMode) getString(R.string.notification_wallgen_title) else getString(R.string.notification_wallimp_title))
                .setContentTitle(this.resources.getString(if (normalMode) R.string.wallet_gen_service_title else R.string.wallet_gen_service_title_import))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentText(getString(R.string.notification_wallgen_maytake))
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(mNotificationId, builder!!.build())
    }

    private fun finished(address: String) {
        builder!!
                .setContentTitle(if (normalMode) getString(R.string.notification_wallgen_finished) else getString(R.string.notification_wallimp_finished))
                .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
                .setAutoCancel(true)
                .setLights(Color.CYAN, 3000, 3000)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(getString(R.string.notification_click_to_view))

        if (android.os.Build.VERSION.SDK_INT >= 18)
        // Android bug in 4.2, just disable it for everyone then...
            builder!!.setVibrate(longArrayOf(1000, 1000))

        val main = Intent(this, MainActivity::class.java)
        main.putExtra("STARTAT", 1)

        val contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT)
        builder!!.setContentIntent(contentIntent)

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(mNotificationId, builder!!.build())
    }


}