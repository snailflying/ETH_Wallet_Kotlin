package cn.mw.ethwallet.network

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import cn.mw.ethwallet.APIKey
import cn.mw.ethwallet.interfaces.LastIconLoaded
import cn.mw.ethwallet.interfaces.StorableWallet
import cn.mw.ethwallet.utils.Key
import okhttp3.*
import java.io.BufferedInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.TimeUnit

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 14:35
 * @description
 */
class EtherscanAPI private constructor() {

    private val token: String

    @Throws(IOException::class)
    fun getPriceChart(starttime: Long, period: Int, usd: Boolean, b: Callback) {
        get("http://poloniex.com/public?command=returnChartData&currencyPair=" + (if (usd) "USDT_ETH" else "BTC_ETH") + "&start=" + starttime + "&end=9999999999&period=" + period, b)
    }


    /**
     * Retrieve all internal transactions from address like contract calls, for normal transactions @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getNormalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    @Throws(IOException::class)
    fun getInternalTransactions(address: String, b: Callback, force: Boolean) {
        if (!force && RequestCache.instance.contains(RequestCache.TYPE_TXS_INTERNAL, address)) {
            b.onResponse(null!!, Response.Builder().code(200).message("").request(Request.Builder()
                    .url("http://api.etherscan.io/api?module=account&action=txlistinternal&address=$address&startblock=0&endblock=99999999&sort=asc&apikey=$token")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.instance.get(RequestCache.TYPE_TXS_INTERNAL, address))).build())
            return
        }
        get("http://api.etherscan.io/api?module=account&action=txlistinternal&address=$address&startblock=0&endblock=99999999&sort=asc&apikey=$token", b)
    }


    /**
     * Retrieve all normal ether transactions from address (excluding contract calls etc, @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getInternalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    @Throws(IOException::class)
    fun getNormalTransactions(address: String, b: Callback, force: Boolean) {
        if (!force && RequestCache.instance.contains(RequestCache.TYPE_TXS_NORMAL, address)) {
            b.onResponse(null!!, Response.Builder().code(200).message("").request(Request.Builder()
                    .url("http://api.etherscan.io/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&sort=asc&apikey=$token")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.instance.get(RequestCache.TYPE_TXS_NORMAL, address))).build())
            return
        }
        get("http://api.etherscan.io/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&sort=asc&apikey=$token", b)
    }


    @Throws(IOException::class)
    fun getEtherPrice(b: Callback) {
        get("http://api.etherscan.io/api?module=stats&action=ethprice&apikey=" + token, b)
    }


    @Throws(IOException::class)
    fun getGasPrice(b: Callback) {
        get("http://api.etherscan.io/api?module=proxy&action=eth_gasPrice&apikey=" + token, b)
    }


    /**
     * Get token balances via ethplorer.io
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    @Throws(IOException::class)
    fun getTokenBalances(address: String, b: Callback, force: Boolean) {
        if (!force && RequestCache.instance.contains(RequestCache.TYPE_TOKEN, address)) {
            b.onResponse(null!!, Response.Builder().code(200).message("").request(Request.Builder()
                    .url("https://api.ethplorer.io/getAddressInfo/$address?apiKey=freekey")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.instance.get(RequestCache.TYPE_TOKEN, address))).build())
            return
        }
        get("http://api.ethplorer.io/getAddressInfo/$address?apiKey=freekey", b)
    }


    /**
     * Download and save token icon in permanent image cache (TokenIconCache)
     *
     * @param c         Application context, used to load TokenIconCache if reinstanced
     * @param tokenName Name of token
     * @param lastToken Boolean defining whether this is the last icon to download or not. If so callback is called to refresh recyclerview (notifyDataSetChanged)
     * @param callback  Callback to @see rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview#onLastIconDownloaded()
     * @throws IOException Network exceptions
     */
    @Throws(IOException::class)
    fun loadTokenIcon(c: Context, tokenName: String, lastToken: Boolean, callback: LastIconLoaded) {
        var tokenName = tokenName
        if (tokenName.indexOf(" ") > 0)
            tokenName = tokenName.substring(0, tokenName.indexOf(" "))
        if (TokenIconCache.getInstance(c).contains(tokenName)) return

        if (tokenName.equals("OMGToken", ignoreCase = true))
            tokenName = "omise"
        else if (tokenName.equals("0x", ignoreCase = true))
            tokenName = "0xtoken_28"

        val tokenNamef = tokenName
        get("http://etherscan.io//token/images/$tokenNamef.PNG", object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (c == null) return
                val `in` = response.body()
                val inputStream = `in`!!.byteStream()
                val bufferedInputStream = BufferedInputStream(inputStream)
                val bitmap = BitmapFactory.decodeStream(bufferedInputStream)
                TokenIconCache.getInstance(c).put(c, tokenNamef, BitmapDrawable(c.resources, bitmap).bitmap)
                // if(lastToken) // TODO: resolve race condition
                callback.onLastIconDownloaded()
            }
        })
    }


    @Throws(IOException::class)
    fun getGasLimitEstimate(to: String, b: Callback) {
        get("http://api.etherscan.io/api?module=proxy&action=eth_estimateGas&to=$to&value=0xff22&gasPrice=0x051da038cc&gas=0xffffff&apikey=$token", b)
    }


    @Throws(IOException::class)
    fun getBalance(address: String, b: Callback) {
        get("http://api.etherscan.io/api?module=account&action=balance&address=$address&apikey=$token", b)
    }


    @Throws(IOException::class)
    fun getNonceForAddress(address: String, b: Callback) {
        get("http://api.etherscan.io/api?module=proxy&action=eth_getTransactionCount&address=$address&tag=latest&apikey=$token", b)
    }


    @Throws(IOException::class)
    fun getPriceConversionRates(currencyConversion: String, b: Callback) {
        get("https://api.fixer.io/latest?base=USD&symbols=" + currencyConversion, b)
    }


    @Throws(IOException::class)
    fun getBalances(addresses: ArrayList<StorableWallet>, b: Callback) {
        var url = "http://api.etherscan.io/api?module=account&action=balancemulti&address="
        for (address in addresses)
            url += address.pubKey + ","
        url = url.substring(0, url.length - 1) + "&tag=latest&apikey=" + token // remove last , AND add token
        get(url, b)
    }


    @Throws(IOException::class)
    fun forwardTransaction(raw: String, b: Callback) {
        get("http://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=$raw&apikey=$token", b)
    }


    @Throws(IOException::class)
    operator fun get(url: String, b: Callback) {
        val request = Request.Builder()
                .url(url)
                .build()
        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

        client.newCall(request).enqueue(b)
    }


    init {
        token = Key(APIKey.API_KEY).toString()
    }

    companion object {

        val instance: EtherscanAPI by lazy { EtherscanAPI() }
//        fun getInstance(): EtherscanAPI {
//            if (instance == null)
//                instance = EtherscanAPI()
//            return instance
//        }
//
    }

}