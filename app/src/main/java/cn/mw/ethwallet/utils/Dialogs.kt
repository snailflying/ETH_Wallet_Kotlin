package cn.mw.ethwallet.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.AddressDetailActivity
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.activities.WalletGenActivity
import cn.mw.ethwallet.domain.request.TokenDisplay
import cn.mw.ethwallet.domain.request.TransactionDisplay
import cn.mw.ethwallet.domain.request.WatchWallet
import cn.mw.ethwallet.fragments.FragmentWallets
import cn.mw.ethwallet.interfaces.AdDialogResponseHandler
import cn.mw.ethwallet.interfaces.PasswordDialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import me.grantland.widget.AutofitTextView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:10
 * @description
 */
object Dialogs {

    fun askForPasswordAndDecode(ac: Activity, fromAddress: String, callback: PasswordDialogCallback) {
        val builder = AlertDialog.Builder(ac, R.style.AlertDialogTheme)
        builder.setTitle("Wallet Password")

        val input = EditText(ac)
        val showpw = CheckBox(ac)
        showpw.setText(R.string.password_in_clear_text)
        input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.transformationMethod = PasswordTransformationMethod.getInstance()

        val container = LinearLayout(ac)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = ac.resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.topMargin = ac.resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.bottomMargin = ac.resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = ac.resources.getDimensionPixelSize(R.dimen.dialog_margin)

        val params2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params2.leftMargin = ac.resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params2.rightMargin = ac.resources.getDimensionPixelSize(R.dimen.dialog_margin)
        input.layoutParams = params
        showpw.layoutParams = params2

        container.addView(input)
        container.addView(showpw)
        builder.setView(container)

        showpw.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked)
                input.transformationMethod = PasswordTransformationMethod.getInstance()
            else
                input.transformationMethod = HideReturnsTransformationMethod.getInstance()
            input.setSelection(input.text.length)
        }

        builder.setView(container)
        input.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {
                    val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    inputMgr!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                }
            }
        }
        builder.setPositiveButton("OK") { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr!!.hideSoftInputFromWindow(input.windowToken, 0)
            callback.success(input.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr!!.hideSoftInputFromWindow(input.windowToken, 0)
            callback.canceled()
            dialog.cancel()
        }

        builder.show()

    }

    fun showTokenetails(c: Activity, tok: TokenDisplay) {
        val dialog = MaterialDialog.Builder(c)
                .customView(R.layout.dialog_token_detail, true)
                .show()
        val view = dialog.getCustomView()
        val contractIcon = view!!.findViewById(R.id.my_addressicon) as ImageView
        val tokenname = view!!.findViewById(R.id.walletname) as TextView
        val contractAddr = view!!.findViewById(R.id.walletaddr) as AutofitTextView

        val supply = view!!.findViewById(R.id.supply) as TextView
        val priceUSD = view!!.findViewById(R.id.price) as TextView
        val priceETH = view!!.findViewById(R.id.price2) as TextView
        val capUSD = view!!.findViewById(R.id.cap) as TextView
        val capETH = view!!.findViewById(R.id.cap2) as TextView
        val holders = view!!.findViewById(R.id.holders) as TextView
        val digits = view!!.findViewById(R.id.digits) as TextView

        val from = view!!.findViewById(R.id.from) as LinearLayout

        from.setOnClickListener {
            val i = Intent(c, AddressDetailActivity::class.java)
            i.putExtra("ADDRESS", tok.contractAddr)
            c.startActivity(i)
        }

        val ex = ExchangeCalculator.instance
        contractIcon.setImageBitmap(Blockies.createIcon(tok.contractAddr!!.toLowerCase()))
        tokenname.setText(tok.name)
        contractAddr.setText(tok.contractAddr!!.toLowerCase())
        supply.setText(ex.displayUsdNicely(tok.totalSupplyLong) + " " + tok.shorty)
        priceUSD.setText(tok.usdprice.toString() + " $")

        priceETH.setText(ex.displayEthNicelyExact(
                ex.convertTokenToEther(1.0, tok.usdprice)
        ) + " " + ex.etherCurrency.shorty)
        capETH.setText(ex.displayUsdNicely(
                ex.convertTokenToEther(tok.totalSupplyLong, tok.usdprice)
        ) + " " + ex.etherCurrency.shorty)
        capUSD.setText(ex.displayUsdNicely(tok.usdprice * tok.totalSupplyLong) + " $")
        holders.setText(ex.displayUsdNicely(tok.holderCount) + "")
        digits.setText(tok.digits.toString() + "")
    }

    fun showTXDetails(c: Activity, tx: TransactionDisplay) {
        val dialog = MaterialDialog.Builder(c)
                .customView(R.layout.dialog_tx_detail, true)
                .show()
        val view = dialog.getCustomView()
        val myicon = view!!.findViewById(R.id.my_addressicon) as ImageView
        val othericon = view!!.findViewById(R.id.other_addressicon) as ImageView
        val myAddressname = view!!.findViewById(R.id.walletname) as TextView
        val otherAddressname = view!!.findViewById(R.id.other_address) as TextView
        val myAddressaddr = view!!.findViewById(R.id.walletaddr) as AutofitTextView
        val otherAddressaddr = view!!.findViewById(R.id.other_addressaddr) as AutofitTextView
        val amount = view!!.findViewById(R.id.amount) as TextView

        val month = view!!.findViewById(R.id.month) as TextView
        val gasUsed = view!!.findViewById(R.id.gasused) as TextView
        val blocknr = view!!.findViewById(R.id.blocknr) as TextView
        val gasPrice = view!!.findViewById(R.id.gasPrice) as TextView
        val nonce = view!!.findViewById(R.id.nonce) as TextView
        val txcost = view!!.findViewById(R.id.txcost) as TextView
        val txcost2 = view!!.findViewById(R.id.txcost2) as TextView
        val openInBrowser = view!!.findViewById(R.id.openinbrowser) as Button
        val from = view!!.findViewById(R.id.from) as LinearLayout
        val to = view!!.findViewById(R.id.to) as LinearLayout
        val amountfiat = view!!.findViewById(R.id.amountfiat) as TextView
        val errormsg = view!!.findViewById(R.id.errormsg) as TextView

        from.setOnClickListener {
            val i = Intent(c, AddressDetailActivity::class.java)
            i.putExtra("ADDRESS", tx.getFromAddress())
            c.startActivity(i)
        }

        to.setOnClickListener {
            val i = Intent(c, AddressDetailActivity::class.java)
            i.putExtra("ADDRESS", tx.getToAddress())
            c.startActivity(i)
        }

        openInBrowser.setOnClickListener {
            val url = "https://etherscan.io/tx/" + tx.txHash
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            c.startActivity(i)
        }

        myicon.setImageBitmap(Blockies.createIcon(tx.getFromAddress().toLowerCase()))
        othericon.setImageBitmap(Blockies.createIcon(tx.getToAddress().toLowerCase()))

        var myName = AddressNameConverter.getInstance(c)[tx.getFromAddress().toLowerCase()]
        if (myName == null) myName = shortName(tx.getFromAddress().toLowerCase())
        var otherName = AddressNameConverter.getInstance(c)[tx.getToAddress().toLowerCase()]
        if (otherName == null) otherName = shortName(tx.getToAddress().toLowerCase())
        myAddressname.text = myName
        otherAddressname.text = otherName

        errormsg.visibility = if (tx.isError) View.VISIBLE else View.GONE
        myAddressaddr.setText(tx.getFromAddress())
        otherAddressaddr.setText(tx.getToAddress())
        val dateformat = SimpleDateFormat("dd. MMMM yyyy, HH:mm:ss", Locale.getDefault())
        month.text = dateformat.format(tx.date) + ""
        blocknr.setText(tx.block.toString() + "")
        gasUsed.setText(tx.gasUsed.toString() + "")
        gasPrice.setText((tx.gasprice / 1000000000).toString() + " Gwei")
        nonce.setText(tx.nounce + "")
        txcost.setText(
                ExchangeCalculator.instance.displayEthNicelyExact(
                        ExchangeCalculator.instance.weiToEther(tx.gasUsed * tx.gasprice)
                ) + " Ξ"
        )
        txcost2.setText(
                (ExchangeCalculator.instance.convertToUsd(ExchangeCalculator.instance.weiToEther(tx.gasUsed * tx.gasprice)).toString()
                        + " " + ExchangeCalculator.instance.mainCurreny.shorty)
        )
        amount.text = (if (tx.amount > 0) "+ " else "- ") + Math.abs(tx.amount) + " Ξ"
        amount.setTextColor(c.resources.getColor(if (tx.amount > 0) R.color.etherReceived else R.color.etherSpent))
        amountfiat.setText((ExchangeCalculator.instance.displayUsdNicely(
                ExchangeCalculator.instance.convertToUsd(tx.amount))
                + " " + ExchangeCalculator.instance.mainCurreny.shorty))
    }

    private fun shortName(addr: String): String {
        return "0x" + addr.substring(2, 8)
    }

    fun addWatchOnly(c: MainActivity) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_watch_only_title)

        val input = EditText(c)
        input.setSingleLine()
        val container = FrameLayout(c)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin)
        params.topMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin)
        params.bottomMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin)
        input.layoutParams = params

        input.inputType = InputType.TYPE_CLASS_TEXT
        container.addView(input)
        builder.setView(container)
        input.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {
                    val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    inputMgr!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                }
            }
        }
        builder.setNegativeButton(R.string.add, DialogInterface.OnClickListener { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr!!.hideSoftInputFromWindow(input.windowToken, 0)
            if (input.text.toString().length == 42 && input.text.toString().startsWith("0x")) {
                val suc = WalletStorage.getInstance(c).add(WatchWallet(input.text.toString()), c)
                Handler().postDelayed(
                        {
                            if (c.fragments != null && c.fragments!![1] != null) {
                                try {
                                    (c.fragments!![1] as FragmentWallets).update()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                            }
                            c.snackError(c.getResources().getString(if (suc) R.string.main_ac_wallet_added_suc else R.string.main_ac_wallet_added_er))
                            if (suc)
                                AddressNameConverter.getInstance(c).put(input.text.toString(), "Watch " + input.text.toString().substring(0, 6), c)
                        }, 100)
            } else {
                c.snackError("Invalid Ethereum address!")
            }
            dialog.dismiss()
        })
        builder.setPositiveButton(R.string.show, DialogInterface.OnClickListener { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr!!.hideSoftInputFromWindow(input.windowToken, 0)
            if (input.text.toString().length == 42 && input.text.toString().startsWith("0x")) {
                val detail = Intent(c, AddressDetailActivity::class.java)
                detail.putExtra("ADDRESS", input.text.toString().toLowerCase())
                c.startActivity(detail)
            } else {
                c.snackError("Invalid Ethereum address!")
            }
            dialog.cancel()
        })
        builder.setNeutralButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr!!.hideSoftInputFromWindow(input.windowToken, 0)
            dialog.cancel()
        })

        builder.show()

    }

    fun writeDownPassword(c: WalletGenActivity) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_write_down_pw_title)
        builder.setMessage(c.getString(R.string.dialog_write_down_pw_text))
        builder.setPositiveButton(R.string.action_sign_in, DialogInterface.OnClickListener { dialog, which ->
            c.gen()
            dialog.cancel()
        })
        builder.setNegativeButton(R.string.dialog_back_button, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    fun importWallets(c: MainActivity, files: ArrayList<File>) {
        var addresses = ""
        var i = 0
        while (i < files.size && i < 3) {
            addresses += WalletStorage.stripWalletName(files[i].name) + "\n"
            i++
        }

        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_importing_wallets_title)
        builder.setMessage(String.format(c.getString(R.string.dialog_importing_wallets_text), files.size, if (files.size > 1) "s" else "", addresses))
        builder.setPositiveButton(R.string.button_yes, DialogInterface.OnClickListener { dialog, which ->
            try {
                WalletStorage.getInstance(c).importWallets(c, files)
                c.snackError("Wallet" + (if (files.size > 1) "s" else "") + " successfully imported!")
                if (c.fragments != null && c.fragments!![1] != null)
                    (c.fragments!![1] as FragmentWallets).update()
            } catch (e: Exception) {
                c.snackError("Error while importing wallets")
                e.printStackTrace()
            }

            dialog.cancel()
        })
        builder.setNegativeButton(R.string.button_no, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    fun adDisable(c: Context, res: AdDialogResponseHandler) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_disable_ad_title)
        builder.setMessage(R.string.dialog_disable_ad_text)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.fragment_recipient_continue, DialogInterface.OnClickListener { dialog, which -> res.continueSettingChange(true) })
        builder.setNegativeButton(R.string.dialog_back_button, DialogInterface.OnClickListener { dialog, which -> res.continueSettingChange(false) })

        builder.show()
    }

    fun cantExportNonWallet(c: Context) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_ex_nofull_title)
        builder.setMessage(R.string.dialog_ex_nofull_text)
        builder.setNeutralButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    fun exportWallet(c: Context, yes: DialogInterface.OnClickListener) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_exporting_title)
        builder.setMessage(R.string.dialog_exporting_text)
        builder.setPositiveButton(R.string.button_ok, yes)
        builder.setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.show()
    }

    fun noFullWallet(c: Context) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_nofullwallet)
        builder.setMessage(R.string.dialog_nofullwallet_text)
        builder.setNeutralButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    fun noWallet(c: Context) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_no_wallets)
        builder.setMessage(R.string.dialog_no_wallets_text)
        builder.setNeutralButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    fun noImportWalletsFound(c: Context) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(c, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(c)
        builder.setTitle(R.string.dialog_no_wallets_found)
        builder.setMessage(R.string.dialog_no_wallets_found_text)
        builder.setNeutralButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }
}