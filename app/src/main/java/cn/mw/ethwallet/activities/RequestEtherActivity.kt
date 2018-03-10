package cn.mw.ethwallet.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.*
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.adapters.WalletAdapter
import cn.mw.ethwallet.domain.request.WalletDisplay
import cn.mw.ethwallet.interfaces.StorableWallet
import cn.mw.ethwallet.utils.qr.AddressEncoder
import cn.mw.ethwallet.utils.qr.Contents
import cn.mw.ethwallet.utils.qr.QREncoder
import cn.mw.ethwallet.utils.AddressNameConverter
import cn.mw.ethwallet.utils.ExchangeCalculator
import cn.mw.ethwallet.utils.WalletStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.activity_requestether.*
import java.math.BigDecimal

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:42
 * @description
 */
class RequestEtherActivity : SecureAppCompatActivity(), View.OnClickListener {

    private var coord: CoordinatorLayout? = null
    //    private var qr: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var walletAdapter: WalletAdapter? = null
    private val wallets = java.util.ArrayList<WalletDisplay>()
    private var selectedEtherAddress: String? = null
    private var amount: TextView? = null
    private var usdPrice: TextView? = null

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requestether)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        coord = findViewById(R.id.main_content) as CoordinatorLayout
//        qr = findViewById(qrcode) as ImageView
        recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        amount = findViewById(R.id.amount) as TextView
        usdPrice = findViewById(R.id.usdPrice) as TextView
        walletAdapter = WalletAdapter(wallets, this, this, this)
        val mgr = LinearLayoutManager(this.applicationContext)
        recyclerView!!.layoutManager = mgr
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = walletAdapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context,
                mgr.orientation)
        recyclerView!!.addItemDecoration(dividerItemDecoration)

        amount!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length != 0) {
                    try {
                        val amountd = java.lang.Double.parseDouble(amount!!.text.toString())
                        usdPrice!!.setText(ExchangeCalculator.instance.displayUsdNicely(ExchangeCalculator.instance.convertToUsd(amountd)) + " " + ExchangeCalculator.instance.mainCurreny.name)
                        updateQR()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        })

        update()
        updateQR()
        if ((this.application as BaseApplication).isGooglePlayBuild) {
            (this.application as BaseApplication).track("Request Activity")
        }
    }


    fun update() {
        wallets.clear()
        val myAddresses = java.util.ArrayList<WalletDisplay>()
        val storedAddresses = ArrayList<StorableWallet>(WalletStorage.getInstance(this).get())
        for (i in storedAddresses.indices) {
            if (i == 0) selectedEtherAddress = storedAddresses.get(i).pubKey
            myAddresses.add(WalletDisplay(
                    AddressNameConverter.getInstance(this).get(storedAddresses.get(i).pubKey)!!,
                    storedAddresses.get(i).pubKey
            ))
        }

        wallets.addAll(myAddresses)
        walletAdapter!!.notifyDataSetChanged()
    }

    fun snackError(s: String) {
        if (coord == null) return
        val mySnackbar = Snackbar.make(coord!!, s, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    fun updateQR() {
        val qrCodeDimention = 400
        var iban = "iban:" + selectedEtherAddress!!
        if (amount!!.text.toString().length > 0 && BigDecimal(amount!!.text.toString()).compareTo(BigDecimal("0")) > 0) {
            iban += "?amount=" + amount!!.text.toString()
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val qrCodeEncoder: QREncoder
        if (prefs.getBoolean("qr_encoding_erc", true)) {
            val temp = AddressEncoder(selectedEtherAddress)
            if (amount!!.text.toString().length > 0 && BigDecimal(amount!!.text.toString()).compareTo(BigDecimal("0")) > 0)
                temp.amount = (amount!!.text.toString())
            qrCodeEncoder = QREncoder(AddressEncoder.encodeERC(temp), null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention)
        } else {
            qrCodeEncoder = QREncoder(iban, null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention)
        }

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

    override fun onClick(view: View) {
        val itemPosition = recyclerView!!.getChildLayoutPosition(view)
        selectedEtherAddress = wallets[itemPosition].publicKey
        updateQR()
    }
}