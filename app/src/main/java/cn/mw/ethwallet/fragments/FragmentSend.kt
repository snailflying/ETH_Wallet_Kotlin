package cn.mw.ethwallet.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SeekBar
import cn.mw.ethwallet.BuildConfig
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.BaseApplication
import cn.mw.ethwallet.activities.SendActivity
import cn.mw.ethwallet.domain.response.Balance
import cn.mw.ethwallet.domain.response.GasPrice
import cn.mw.ethwallet.interfaces.PasswordDialogCallback
import cn.mw.ethwallet.network.EtherscanAPI1
import cn.mw.ethwallet.services.TransactionService
import cn.mw.ethwallet.utils.*
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_send.*
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:03
 * @description
 */
class FragmentSend : Fragment() {
    val TAG = "FragmentSend"

    private val DEFAULT_GAS_PRICE = 12

    private var ac: SendActivity? = null
    //    private var send: Button? = null
//    private var amount: EditText? = null
//    private var toAddress: TextView? = null
//    private var toName: TextView? = null
//    private var usdPrice: TextView? = null
//    private var gasText: TextView? = null
//    private var fromName: TextView? = null
//    private var availableEth: TextView? = null
//    private var availableFiat: TextView? = null
//    private var availableFiatSymbol: TextView? = null
//    private var txCost: TextView? = null
//    private var txCostFiat: TextView? = null
//    private var txCostFiatSymbol: TextView? = null
//    private var totalCost: TextView? = null
//    private var totalCostFiat: TextView? = null
//    private var totalCostFiatSymbol: TextView? = null
//    private var gas: SeekBar? = null
//    private var toicon: ImageView? = null
//    private var fromicon: ImageView? = null
//    private var spinner: Spinner? = null
//    private var currencySpinner: Spinner? = null
    private var amountInEther = true
    private var gaslimit = BigInteger("21000")
    private var curAvailable = BigDecimal.ZERO
    private var curTxCost = BigDecimal("0.000252")
    private var curAmount = BigDecimal.ZERO
    private val exchange = ExchangeCalculator.instance
    //    private var expertMode: LinearLayout? = null
//    private var data: EditText? = null
//    private var userGasLimit: EditText? = null
    private var realGas: Double = 0.toDouble()

    private val curTotalCost: BigDecimal
        get() = curAmount.add(curTxCost, MathContext.DECIMAL64)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_send, container, false)

        ac = this.activity as SendActivity

//        send = rootView.findViewById(R.id.send)
//        amount = rootView.findViewById(R.id.amount)
//        gas = rootView.findViewById(seekBar)
//        toAddress = rootView.findViewById(R.id.toAddress)
//        toName = rootView.findViewById(R.id.toName)
//        fromName = rootView.findViewById(R.id.fromName)
//        usdPrice = rootView.findViewById(R.id.usdPrice)

//        availableEth = rootView.findViewById(R.id.ethAvailable)
//        availableFiat = rootView.findViewById(R.id.ethAvailableFiat)
//        availableFiatSymbol = rootView.findViewById(R.id.ethAvailableFiatSymbol)

//        txCost = rootView.findViewById(R.id.txCost)
//        txCostFiat = rootView.findViewById(R.id.txCostFiat)
//        txCostFiatSymbol = rootView.findViewById(R.id.txCostFiatSymbol)

//        totalCost = rootView.findViewById(R.id.totalCost)
//        totalCostFiat = rootView.findViewById(R.id.totalCostFiat)
//        totalCostFiatSymbol = rootView.findViewById(R.id.totalCostFiatSymbol)

//        gasText = rootView.findViewById(R.id.gasText)
//        toicon = rootView.findViewById(R.id.toicon)
//        fromicon = rootView.findViewById(R.id.fromicon)
//        expertMode = rootView.findViewById(R.id.expertmode)
//        data = rootView.findViewById(R.id.data)
//        userGasLimit = rootView.findViewById(R.id.userGasLimit)

        (rootView.findViewById(R.id.expertmodetrigger) as LinearLayout).setOnClickListener {
            if (expertMode!!.visibility == View.GONE) {
                CollapseAnimator.expand(expertMode)
            } else {
                CollapseAnimator.collapse(expertMode)
            }
        }

        if (arguments!!.containsKey("TO_ADDRESS")) {
            setToAddress(arguments!!.getString("TO_ADDRESS"), ac!!)
        }

        if (arguments!!.containsKey("AMOUNT")) {
            curAmount = BigDecimal(arguments!!.getString("AMOUNT"))
            amount!!.setText(arguments!!.getString("AMOUNT"))
        }

        gas.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                realGas = (i - 8).toDouble()
                if (i < 10)
                    realGas = (i + 1).toDouble() / 10.0

                gasText!!.text = (realGas.toString() + "").replace(".0".toRegex(), "")
                curTxCost = BigDecimal(gaslimit).multiply(BigDecimal(realGas.toString() + "")).divide(BigDecimal("1000000000"), 6, BigDecimal.ROUND_DOWN)
                updateDisplays()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        gas!!.progress = DEFAULT_GAS_PRICE

//        spinner = rootView.findViewById(R.id.spinner)
        val spinnerArrayAdapter = object : ArrayAdapter<String>(ac, R.layout.address_spinner, WalletStorage.getInstance(ac!!).fullOnly) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
                return view
            }
        }
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.adapter = spinnerArrayAdapter

        spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                updateAccountBalance()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        amount!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateAmount(s.toString())
                updateDisplays()
            }
        })

//        currencySpinner = rootView.findViewById(R.id.currencySpinner)
        val currencyList = ArrayList<String>()
        currencyList.add("ETH")
        currencyList.add(ExchangeCalculator.instance.mainCurreny.name)
        val curAdapter = ArrayAdapter(ac, android.R.layout.simple_spinner_item, currencyList)
        curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner!!.adapter = curAdapter
        currencySpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                amountInEther = i == 0

                updateAmount(amount!!.text.toString())
                updateDisplays()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        send!!.setOnClickListener(View.OnClickListener {
            if ((amount!!.text.length <= 0 || BigDecimal(amount!!.text.toString()).compareTo(BigDecimal("0")) <= 0) && data!!.text.length <= 0) {
                ac!!.snackError(getString(R.string.err_send_noamount))
                return@OnClickListener
            }
            if (toAddress == null || toAddress!!.text.length == 0) {
                ac!!.snackError(getString(R.string.err_send_noreceiver))
                return@OnClickListener
            }
            if (spinner == null || spinner!!.selectedItem == null) return@OnClickListener
            try {
                if (BuildConfig.DEBUG)
                    Log.d("etherbalance", (curTotalCost.compareTo(curAvailable) < 0).toString() + " | " + curTotalCost + " | " + curAvailable + " | " + data!!.text + " | " + curAmount)
                if (curTotalCost.compareTo(curAvailable) < 0 || BuildConfig.DEBUG || data!!.text.length > 0) {
                    Dialogs.askForPasswordAndDecode(ac!!, spinner!!.selectedItem.toString(), object : PasswordDialogCallback {

                        override fun success(password: String) {
                            sendEther(password, spinner!!.selectedItem.toString())
                        }

                        override fun canceled() {}
                    })
                } else {
                    ac!!.snackError(getString(R.string.err_send_not_enough_ether))
                }
            } catch (e: Exception) {
                ac!!.snackError(getString(R.string.err_send_invalidamount))
            }
        })

        if (arguments!!.containsKey("FROM_ADDRESS")) {
            setFromAddress(arguments!!.getString("FROM_ADDRESS"))
        }

        updateAccountBalance()
        updateDisplays()

        if ((ac!!.getApplication() as BaseApplication).isGooglePlayBuild) {
            (ac!!.getApplication() as BaseApplication).track("Send Fragment")
        }

        return rootView
    }

    private fun updateAccountBalance() {
        /*try {
            EtherscanAPI.instance.getBalance(spinner!!.selectedItem.toString(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ac!!.runOnUiThread(Runnable { ac!!.snackError("Cant fetch your account balance", Snackbar.LENGTH_LONG) })

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    ac!!.runOnUiThread(Runnable {
                        try {
                            curAvailable = BigDecimal(ResponseParser.parseBalance(response.body()!!.string(), 6))
                            updateDisplays()
                        } catch (e: Exception) {
                            ac!!.snackError("Cant fetch your account balance")
                            e.printStackTrace()
                        }
                    })
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }*/
        EtherscanAPI1.instance.getBalance(ac!!, spinner!!.selectedItem.toString())
                .subscribe(
                        object : SingleObserver<Balance> {
                            override fun onSuccess(t: Balance) {
                                val balance = if (t.result == "0") "0" else BigDecimal(t.result).divide(BigDecimal(1000000000000000000.0), 6, BigDecimal.ROUND_UP).toPlainString()
                                curAvailable = BigDecimal(balance)
                                updateDisplays()
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                ac!!.snackError("Cant fetch your account balance", Snackbar.LENGTH_LONG)
                            }

                        }

                )
        fromicon!!.setImageBitmap(Blockies.createIcon(spinner!!.selectedItem.toString().toLowerCase()))
        fromName!!.setText(AddressNameConverter.getInstance(ac!!).get(spinner!!.selectedItem.toString().toLowerCase()))
    }

    private fun setFromAddress(from: String?) {
        val fullwallets = WalletStorage.getInstance(ac!!).fullOnly
        for (i in fullwallets.indices) {
            if (fullwallets.get(i).equals(from!!, ignoreCase = true)) {
                spinner!!.setSelection(i)
            }
        }
    }

    private fun updateDisplays() {
        updateAvailableDisplay()
        updateAmountDisplay()
        updateTxCostDisplay()
        updateTotalCostDisplay()
    }

    private fun updateAvailableDisplay() {
        exchange.index = (2)

        ethAvailable!!.text = curAvailable.toString()
        ethAvailableFiat!!.setText(exchange.convertRateExact(curAvailable, exchange.usdPrice))
        ethAvailableFiatSymbol!!.setText(exchange.current.shorty)
    }

    private fun updateAmount(str: String) {
        try {
            val origA = BigDecimal(str)

            if (amountInEther) {
                curAmount = origA
            } else {
                curAmount = origA.divide(BigDecimal(exchange.usdPrice), 7, RoundingMode.FLOOR)
            }
        } catch (e: NumberFormatException) {
            curAmount = BigDecimal.ZERO
        }

    }

    private fun updateAmountDisplay() {
        val price: String
        if (amountInEther) {
            price = exchange.convertRateExact(curAmount, exchange.usdPrice) +
                    " " + exchange.mainCurreny.name
        } else {
            exchange.index = (0)
            price = curAmount.toPlainString() + " " + exchange.current.shorty
        }

        usdPrice!!.text = price
    }

    private fun updateTxCostDisplay() {
        exchange.index = (2)

        txCost!!.text = curTxCost.toString()
        txCostFiat!!.setText(exchange.convertRateExact(curTxCost, exchange.usdPrice))
        txCostFiatSymbol!!.setText(exchange.current.shorty)
    }

    private fun updateTotalCostDisplay() {
        exchange.index = (2)

        val curTotalCost = curTotalCost

        totalCost!!.text = curTotalCost.toString()
        totalCostFiat!!.setText(exchange.convertRateExact(curTotalCost, exchange.usdPrice))
        totalCostFiatSymbol!!.setText(exchange.current.shorty)
    }

    private fun getEstimatedGasPriceLimit() {
        try {
            /*EtherscanAPI.instance.getGasLimitEstimate(toAddress!!.text.toString(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @SuppressLint("SetTextI18n")
                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        gaslimit = ResponseParser.parseGasPrice(response.body()!!.string())
                        ac!!.runOnUiThread(Runnable { userGasLimit.setText(gaslimit.toString()) })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })*/
            EtherscanAPI1.instance.getGasLimitEstimate(ac!!, toAddress!!.text.toString())
                    .subscribe(
                            object : SingleObserver<GasPrice> {
                                @SuppressLint("SetTextI18n")
                                override fun onSuccess(t: GasPrice) {
                                    gaslimit = BigInteger(t.result.substring(2), 16)
                                    ac!!.runOnUiThread(Runnable { userGasLimit.setText(gaslimit.toString()) })
                                }

                                override fun onSubscribe(d: Disposable) {
                                }

                                override fun onError(e: Throwable) {
                                }

                            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun sendEther(password: String, fromAddress: String) {
        val txService = Intent(ac, TransactionService::class.java)
        txService.putExtra("FROM_ADDRESS", fromAddress)
        txService.putExtra("TO_ADDRESS", toAddress!!.text.toString())
        txService.putExtra("AMOUNT", curAmount.toPlainString()) // In ether, gets converted by the service itself
        txService.putExtra("GAS_PRICE", BigDecimal(realGas.toString() + "").multiply(BigDecimal("1000000000")).toBigInteger().toString())// "21000000000");
        txService.putExtra("GAS_LIMIT", if (userGasLimit!!.text.length <= 0) gaslimit.toString() else userGasLimit!!.text.toString())
        txService.putExtra("PASSWORD", password)
        txService.putExtra("DATA", data!!.text.toString())
        ac!!.startService(txService)

        // For statistics
        if ((ac!!.getApplication() as BaseApplication).isGooglePlayBuild) {
            (ac!!.getApplication() as BaseApplication).event("Send Ether")
        }

        val data = Intent()
        data.putExtra("FROM_ADDRESS", fromAddress)
        data.putExtra("TO_ADDRESS", toAddress!!.text.toString())
        data.putExtra("AMOUNT", curAmount.toPlainString())
        ac!!.setResult(RESULT_OK, data)
        ac!!.finish()
    }

    fun setToAddress(to: String, c: Context) {
        if (toAddress == null) return
        toAddress!!.text = to
        val name = AddressNameConverter.getInstance(c).get(to)
        toName!!.text = name ?: to!!.substring(0, 10)
        toicon!!.setImageBitmap(Blockies.createIcon(to!!.toLowerCase()))
        getEstimatedGasPriceLimit()
    }
}