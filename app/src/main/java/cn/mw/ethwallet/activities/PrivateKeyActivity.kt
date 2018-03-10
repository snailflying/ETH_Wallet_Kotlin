package cn.mw.ethwallet.activities

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.utils.qr.Contents
import cn.mw.ethwallet.utils.qr.QREncoder
import cn.mw.ethwallet.utils.WalletStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.activity_privatekey.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 19:46
 * @description
 */
class PrivateKeyActivity : SecureAppCompatActivity() {

    //    private var qr: ImageView? = null
    private var privateKey: TextView? = null
    private var progress: ProgressBar? = null

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privatekey)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

//        qr = findViewById(qrcode) as ImageView
        privateKey = findViewById(R.id.privateKey) as TextView
        progress = findViewById(R.id.progressBar) as ProgressBar

        getPrivateKey(intent.getStringExtra(PASSWORD), intent.getStringExtra(ADDRESS))
        if ((this.application as BaseApplication).isGooglePlayBuild) {
            (this.application as BaseApplication).track("Private Key Activity")
        }
    }

    private fun getPrivateKey(password: String, address: String) {
        object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg params: String): String? {
                try {
                    val keys = WalletStorage.getInstance(applicationContext).getFullWallet(applicationContext, params[0], params[1])
                    return keys.getEcKeyPair().getPrivateKey().toString(16)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(result: String) {
                progress!!.visibility = View.GONE
                qrcode!!.visibility = View.VISIBLE
                update(result)
            }
        }.execute(password, address)

    }

    fun update(key: String?) {
        if (key != null) {
            privateKey!!.text = key
            updateQR(key)
        } else {
            privateKey!!.text = getString(R.string.activity_private_key_wrong_pw)
            // Wrong key
        }
    }

    fun updateQR(privateKey: String) {
        val qrCodeDimention = 400
        val qrCodeEncoder = QREncoder(privateKey, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention)

        try {
            val bitmap = qrCodeEncoder.encodeAsBitmap()
            qrcode!!.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {

        val ADDRESS = "ADDRESS"
        val PASSWORD = "PASSWORD"
    }

}