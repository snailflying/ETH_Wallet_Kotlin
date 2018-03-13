package cn.mw.ethwallet.utils

import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.mw.ethwallet.domain.mod.CurrencyEntry
import cn.mw.ethwallet.domain.response.TokenDisplay
import cn.mw.ethwallet.interfaces.NetworkUpdateListener
import cn.mw.ethwallet.network.EtherscanAPI1
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 14:21
 * @description
 */
class ExchangeCalculator private constructor() {
    private val TAG = "ExchangeCalculator"

    private val lastUpdateTimestamp: Long = 0
    var rateForChartDisplay = 1.0
        private set
    private val formatterUsd = DecimalFormat("#,###,###.##")
    private val formatterCrypt = DecimalFormat("#,###,###.####")
    private val formatterCryptExact = DecimalFormat("#,###,###.#######")

    private val conversionNames = arrayOf<CurrencyEntry>(CurrencyEntry("ETH", 1.0, "Ξ"), CurrencyEntry("BTC", 0.07, "฿"), CurrencyEntry("USD", 0.0, "$"))

    var index = 0

    val current: CurrencyEntry
        get() = conversionNames[index]

    val mainCurreny: CurrencyEntry
        get() = conversionNames[2]

    val etherCurrency: CurrencyEntry
        get() = conversionNames[0]

    val currencyShort: String
        get() = conversionNames[index].shorty

    val usdPrice: Double
        get() = Math.floor(conversionNames[2].rate * 100) / 100

    val btcPrice: Double
        get() = Math.floor(conversionNames[1].rate * 10000) / 10000

    operator fun next(): CurrencyEntry {
        index = (index + 1) % conversionNames.size
        return conversionNames[index]
    }

    fun previous(): CurrencyEntry {
        index = if (index > 0) index - 1 else conversionNames.size - 1
        return conversionNames[index]
    }

    fun displayBalanceNicely(d: Double): String {
        return if (index == 2)
            displayUsdNicely(d)
        else
            displayEthNicely(d)
    }

    fun displayUsdNicely(d: Double): String {
        return formatterUsd.format(d)
    }

    fun displayEthNicely(d: Double): String {
        return formatterCrypt.format(d)
    }

    fun displayEthNicelyExact(d: Double): String {
        return formatterCryptExact.format(d)
    }

    /**
     * Converts given tokenbalance to ETH
     *
     * @param tokenbalance native token balance
     * @param tokenusd     price in USD for each token
     * @return Ether worth of given tokens
     */
    fun convertTokenToEther(tokenbalance: Double, tokenusd: Double): Double {
        return Math.floor(tokenbalance * tokenusd / conversionNames[2].rate * 10000) / 10000
    }

    fun convertRate(balance: Double, rate: Double): Double {
        if (index == 2) {
            return if (balance * rate >= 100000) Math.floor(balance * rate).toInt().toDouble() else Math.floor(balance * rate * 100.0) / 100
        } else {
            if (balance * rate >= 1000)
                return Math.floor(balance * rate * 10.0) / 10
            return if (balance * rate >= 100) Math.floor(balance * rate * 100.0) / 100 else Math.floor(balance * rate * 1000.0) / 1000
        }
    }

    fun weiToEther(weis: Long): Double {
        return BigDecimal(weis).divide(ONE_ETHER, 8, BigDecimal.ROUND_DOWN).toDouble()
    }

    fun convertRateExact(balance: BigDecimal, rate: Double): String {
        return if (index == 2) {
            displayUsdNicely(Math.floor(balance.toDouble() * rate * 100.0) / 100) + ""
        } else
            displayEthNicelyExact(balance.multiply(BigDecimal(rate)).setScale(7, RoundingMode.CEILING).toDouble())
    }

    fun convertToUsd(balance: Double): Double {
        return Math.floor(balance * usdPrice * 100.0) / 100
    }

    /**
     * Used for DetailFragmentOverview "Overall Balance"
     *
     * @param token List of all tokens on an address
     * @return ether price of all tokens combined (exclusive ether balance itself)
     */
    fun sumUpTokenEther(token: List<TokenDisplay>): Double {
        var summedEther = 0.0
        for (t in token) {
            if (t.shorty.equals("ETH")) continue
            summedEther += convertTokenToEther(t.balanceDouble, t.usdprice)
        }
        return summedEther
    }

    @Throws(IOException::class)
    fun updateExchangeRates(activity: AppCompatActivity, currency: String, update: NetworkUpdateListener) {
        if (lastUpdateTimestamp + 40 * 60 * 1000 > System.currentTimeMillis() && currency == conversionNames[2].name) { // Dont refresh if not older than 40 min and currency hasnt changed
            return
        }
        if (currency != conversionNames[2].name) {
            conversionNames[2].name = currency
            if (currency == "USD")
                conversionNames[2].shorty = "$"
            else if (currency == "EUR")
                conversionNames[2].shorty = "€"
            else if (currency == "GPB")
                conversionNames[2].shorty = "£"
            else if (currency == "AUD")
                conversionNames[2].shorty = "$"
            else if (currency == "RUB")
                conversionNames[2].shorty = "р"
            else if (currency == "CHF")
                conversionNames[2].shorty = "Fr"
            else if (currency == "CAD")
                conversionNames[2].shorty = "$"
            else if (currency == "JPY")
                conversionNames[2].shorty = "¥"
            else
                conversionNames[2].shorty = currency
        }

        //Log.d("updateingn", "Initialize price update");
        /*EtherscanAPI.instance.getEtherPrice(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = JSONObject(response.body()!!.string()).getJSONObject("result")

                    conversionNames[1].rate = data.getDouble("ethbtc")
                    conversionNames[2].rate = data.getDouble("ethusd")
                    if (currency != "USD")
                        convert(currency, update)
                    else
                        update.onUpdate()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })*/
        EtherscanAPI1.instance.getEtherPrice(activity)
                .subscribe({
                    conversionNames[1].rate = it.result.ethbtc
                    conversionNames[2].rate = it.result.ethusd
                    if (currency != "USD")
                        convert(currency, update)
                    else
                        update.onUpdate()
                }, {
                    Log.e(TAG, "throw:" + it)

                })
    }

    @Throws(IOException::class)
    private fun convert(currency: String, update: NetworkUpdateListener) {
//        EtherscanAPI.instance.getPriceConversionRates(currency, object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//
//            @Throws(IOException::class)
//            override fun onResponse(call: Call, response: Response) {
//
//                rateForChartDisplay = ResponseParser.parsePriceConversionRate(response.body()!!.string())
//                conversionNames[2].rate =(Math.floor(conversionNames[2].rate * rateForChartDisplay * 100) / 100)
//                update.onUpdate()
//            }
//        })
        EtherscanAPI1.instance.getPriceConversionRates(currency)
                .subscribe({
                    rateForChartDisplay = it.rates
                    conversionNames[2].rate = (Math.floor(conversionNames[2].rate * rateForChartDisplay * 100) / 100)
                    update.onUpdate()
                }, {
                    Log.e(TAG, "throw:" + it)
                })

    }

    companion object {

        val ONE_ETHER = BigDecimal("1000000000000000000")

        val instance: ExchangeCalculator by lazy { ExchangeCalculator() }

    }

}