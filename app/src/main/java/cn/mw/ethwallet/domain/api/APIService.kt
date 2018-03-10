package cn.mw.ethwallet.domain.api

import cn.mw.ethwallet.domain.request.FullWallet
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 2017/8/15 19:07
 * @description
 */
interface APIService {

    companion object {

        const val TOKEN_BALANCE_URL = "https://api.ethplorer.io/"
        const val PRICE_CHART_URL = "http://poloniex.com/"
        const val TRANSACTION_URL = "http://api.etherscan.io/"
        const val API_BASE_SERVER_URL = TOKEN_BALANCE_URL
    }

    @GET(PRICE_CHART_URL + "public")
    abstract fun getPriceChart(@QueryMap queryMap: Map<String, String>): Single<FullWallet>
    /*微信相关接口  end*/

}
