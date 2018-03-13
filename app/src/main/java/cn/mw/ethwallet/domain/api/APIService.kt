package cn.mw.ethwallet.domain.api

import cn.mw.ethwallet.APIKey
import cn.mw.ethwallet.domain.response.*
import cn.mw.ethwallet.utils.Key
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 2017/8/15 19:07
 * @description
 */
interface APIService {

    companion object {

        const val API_BASE_SERVER_URL = "http://api.etherscan.io/"
        val token: String = Key(APIKey.API_KEY).toString()

    }

    @GET("api?module=account&action=txlistinternal&startblock=0&endblock=99999999&sort=asc")
    fun getInternalTransactions(@Query("address") queryAddress: String, @Query("apikey") queryToken: String = token): Single<String>

    @GET("api?module=account&action=txlist&startblock=0&endblock=99999999&sort=asc")
    fun getNormalTransactions(@Query("address") queryAddress: String, @Query("apikey") queryToken: String = token): Single<String>

    @GET("api?module=stats&action=ethprice")
    fun getEtherPrice(@Query("apikey") queryValue: String = token): Single<EtherPrice>

    //暂未用到
    @GET("api?module=proxy&action=eth_gasPrice")
    fun getGasPrice( @Query("apikey") queryValue: String = token): Single<Price>

    @GET("token/images/{tokenNamef}.PNG")
    fun loadTokenIcon(@Path("tokenNamef") tokenNamef: String): Single<ResponseBody>

    @GET("api?module=proxy&action=eth_estimateGas&value=0xff22&gasPrice=0x051da038cc&gas=0xffffff")
    fun getGasLimitEstimate(@Query("to") to: String, @Query("apikey") queryValue: String = token): Single<GasPrice>

    @GET("api?module=account&action=balance")
    fun getBalance(@Query("address") address: String, @Query("apikey") queryValue: String = token): Single<Balance>


    @GET("api?module=proxy&action=eth_getTransactionCount&tag=latest")
    fun getNonceForAddress(@Query("address") address: String, @Query("apikey") queryValue: String = token): Single<NonceForAddress>

    @GET("api?module=account&action=balancemulti&tag=latest")
    fun getBalances(@Query("address") address: String, @Query("apikey") queryValue: String = token): Single<String>

    @GET("api?module=proxy&action=eth_sendRawTransaction")
    fun forwardTransaction(@Query("hex") raw: String, @Query("apikey") queryValue: String = token): Single<ForwardTX>


    @GET("https://poloniex.com/public?command=returnChartData")
    fun getPriceChart(@QueryMap queryMap: Map<String, String>): Single<List<PriceChart>>

    @GET("https://api.fixer.io/latest?base=USD")
    fun getPriceConversionRates(@Query("symbols") currencyConversion: String): Single<Rate>

    @GET("https://api.ethplorer.io/getAddressInfo/{address}?apiKey=freekey")
    fun getTokenBalances(@Path("address") address: String): Single<List<TokenDisplay>>


}
