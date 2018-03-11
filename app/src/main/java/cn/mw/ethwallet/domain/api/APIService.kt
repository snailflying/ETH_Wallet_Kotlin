package cn.mw.ethwallet.domain.api

import cn.mw.ethwallet.APIKey
import cn.mw.ethwallet.domain.request.FullWallet
import cn.mw.ethwallet.domain.response.PriceChart
import cn.mw.ethwallet.utils.Key
import io.reactivex.Single
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

    @GET("api")
    fun getInternalTransactions(@QueryMap queryMap: Map<String, String>): Single<FullWallet>

    @GET("api")
    fun getNormalTransactions(@QueryMap queryMap: Map<String, String>): Single<FullWallet>

    @GET("api")
    fun getEtherPrice(@QueryMap queryMap: Map<String, String>): Single<FullWallet>

    @GET("api")
    fun getGasPrice(@QueryMap queryMap: Map<String, String>): Single<FullWallet>

    @GET("api")
    fun loadTokenIcon(@QueryMap queryMap: Map<String, String>): Single<FullWallet>


    @GET("api")
    fun getGasLimitEstimate(@QueryMap queryMap: Map<String, String>): Single<FullWallet>


    @GET("api")
    fun getBalance(@QueryMap queryMap: Map<String, String>): Single<FullWallet>


    @GET("api")
    fun getNonceForAddress(@QueryMap queryMap: Map<String, String>): Single<FullWallet>


    @GET("api")
    fun getBalances(@QueryMap queryMap: Map<String, String>): Single<FullWallet>

    @GET("api")
    fun forwardTransaction(@QueryMap queryMap: Map<String, String>): Single<FullWallet>


    @GET("http://poloniex.com/public?command=returnChartData")
    fun getPriceChart(@QueryMap queryMap: Map<String, String>): Single<List<PriceChart>>

    @GET("https://api.fixer.io/latest?base=USD")
    fun getPriceConversionRates(@Query("symbols") currencyConversion: String): Single<FullWallet>

    @GET("https://api.ethplorer.io/getAddressInfo/{address}?apiKey=freekey")
    fun getTokenBalances(@Path("address") address: String): Single<FullWallet>


}
