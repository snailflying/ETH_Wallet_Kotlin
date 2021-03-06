package cn.mw.ethwallet.services

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.domain.response.ForwardTX
import cn.mw.ethwallet.domain.response.NonceForAddress
import cn.mw.ethwallet.network.EtherscanAPI
import cn.mw.ethwallet.utils.ExchangeCalculator
import cn.mw.ethwallet.utils.WalletStorage
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.methods.request.RawTransaction
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:21
 * @description
 */


class TransactionService : IntentService("Transaction Service") {

    private var builder: NotificationCompat.Builder? = null
    internal val mNotificationId = 153

    override fun onHandleIntent(intent: Intent?) {
        sendNotification()
        try {
            val fromAddress = intent!!.getStringExtra("FROM_ADDRESS")
            val toAddress = intent.getStringExtra("TO_ADDRESS")
            val amount = intent.getStringExtra("AMOUNT")
            val gas_price = intent.getStringExtra("GAS_PRICE")
            val gas_limit = intent.getStringExtra("GAS_LIMIT")
            val data = intent.getStringExtra("DATA")
            val password = intent.getStringExtra("PASSWORD")

            val keys = WalletStorage.getInstance(applicationContext).getFullWallet(applicationContext, password, fromAddress)

            EtherscanAPI.INSTANCE.getNonceForAddress(fromAddress)
                    .subscribe(
                            object : SingleObserver<NonceForAddress> {
                                override fun onSuccess(t: NonceForAddress) {
                                    if (t.result.length < 2) return

                                    val nonce = BigInteger(t.result.substring(2), 16)

                                    val tx = RawTransaction.createTransaction(
                                            nonce,
                                            BigInteger(gas_price),
                                            BigInteger(gas_limit),
                                            toAddress,
                                            BigDecimal(amount).multiply(ExchangeCalculator.ONE_ETHER).toBigInteger(),
                                            data
                                    )

                                    Log.d("Aaron",
                                            "Nonce: " + tx.nonce + "\n" +
                                                    "gasPrice: " + tx.gasPrice + "\n" +
                                                    "gasLimit: " + tx.gasLimit + "\n" +
                                                    "To: " + tx.to + "\n" +
                                                    "Amount: " + tx.value + "\n" +
                                                    "Data: " + tx.data
                                    )

                                    val signed = TransactionEncoder.signMessage(tx, 1.toByte(), keys)

                                    forwardTX(signed)
                                }

                                override fun onSubscribe(d: Disposable) {
                                }

                                override fun onError(e: Throwable) {
                                    error("Can't connect to network, retry it later")
                                }

                            }
                    )

        } catch (e: Exception) {
            error("Invalid Wallet Password!")
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun forwardTX(signed: ByteArray) {

        EtherscanAPI.INSTANCE.forwardTransaction("0x" + Hex.toHexString(signed))
                .subscribe(
                        object : SingleObserver<ForwardTX> {
                            override fun onSuccess(t: ForwardTX) {
                                if (!TextUtils.isEmpty(t.result)) {
                                    suc(t.result)
                                } else {
                                    var errormsg = t.error.message
                                    if (errormsg.indexOf(".") > 0)
                                        errormsg = errormsg.substring(0, errormsg.indexOf("."))
                                    error(errormsg) // f.E Insufficient funds
                                }
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                error("Can't connect to network, retry it later")
                            }


                        }
                )
    }

    private fun suc(hash: String) {
        builder!!
                .setContentTitle(getString(R.string.notification_transfersuc))
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText("")

        val main = Intent(this, MainActivity::class.java)
        main.putExtra("STARTAT", 2)
        main.putExtra("TXHASH", hash)

        val contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT)
        builder!!.setContentIntent(contentIntent)

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(mNotificationId, builder!!.build())
    }

    private fun error(err: String) {
        builder!!
                .setContentTitle(getString(R.string.notification_transferfail))
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(err)

        val main = Intent(this, MainActivity::class.java)
        main.putExtra("STARTAT", 2)

        val contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT)
        builder!!.setContentIntent(contentIntent)

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(mNotificationId, builder!!.build())
    }

    private fun sendNotification() {
        builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transferingticker))
                .setContentTitle(getString(R.string.notification_transfering_title))
                .setContentText(getString(R.string.notification_might_take_a_minute))
                .setOngoing(true)
                .setProgress(0, 0, true)
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(mNotificationId, builder!!.build())
    }


}