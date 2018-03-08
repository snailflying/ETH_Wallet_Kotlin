package cn.mw.ethwallet.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import cn.mw.ethwallet.R
import cn.mw.ethwallet.qr.AddressEncoder
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.IOException
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:51
 * @description
 */
class QRScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var type: Byte = 0

    private var mScannerView: ZXingScannerView? = null
    private var barCode: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan)


        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val title = findViewById(R.id.toolbar_title) as TextView
        type = intent.getByteExtra("TYPE", SCAN_ONLY)

        title.text = if (type == SCAN_ONLY) "Scan Address" else "ADD WALLET"

        barCode = findViewById(R.id.barcode) as FrameLayout
        // BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        // barcodeCapture.setRetrieval(this);

        if (hasPermission(this))
            initQRScan(barCode)
        else
            askForPermissionRead(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun initQRScan(frame: FrameLayout?) {
        mScannerView = ZXingScannerView(this)
        frame!!.addView(mScannerView)
        mScannerView!!.setResultHandler(this)
        val supported = ArrayList<BarcodeFormat>()
        supported.add(BarcodeFormat.QR_CODE)
        mScannerView!!.setFormats(supported)
        mScannerView!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        if (mScannerView != null)
            mScannerView!!.stopCamera()
    }

    fun hasPermission(c: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initQRScan(barCode)
                } else {
                    Toast.makeText(this, "Please grant camera permission in order to read QR codes", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun handleResult(result: Result?) {
        if (result == null) return
        val address = result.text
        try {
            val scanned = AddressEncoder.decode(address)
            val data = Intent()
            data.putExtra("ADDRESS", scanned.address!!.toLowerCase())

            if (scanned.address!!.length > 42 && !scanned.address!!.startsWith("0x") && scanned.amount == null)
                type = PRIVATE_KEY

            if (scanned.amount != null) {
                data.putExtra("AMOUNT", scanned.amount)
                type = REQUEST_PAYMENT
            }

            data.putExtra("TYPE", type)
            setResult(Activity.RESULT_OK, data)
            finish()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {

        val REQUEST_CODE = 100
        val REQUEST_CAMERA_PERMISSION = 106

        val SCAN_ONLY: Byte = 0
        val ADD_TO_WALLETS: Byte = 1
        val REQUEST_PAYMENT: Byte = 2
        val PRIVATE_KEY: Byte = 3

        fun askForPermissionRead(c: Activity) {
            if (Build.VERSION.SDK_INT < 23) return
            ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

}