package cn.mw.ethwallet.network

import android.support.v7.app.AppCompatActivity
import cn.mw.ethwallet.domain.api.APIService
import cn.mw.ethwallet.domain.response.PriceChart
import cn.mw.ethwallet.utils.cache.Cache
import com.safframework.lifecycle.RxLifecycle
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 11/03/2018 09:55
 * @description
 */
class EtherscanAPI1 private constructor() {


    companion object {
        val instance: EtherscanAPI1 by lazy { EtherscanAPI1() }
        val cacheKey = "cacheKey"
    }

    fun getPriceChart(activity: AppCompatActivity, startTime: Long, period: Int, usd: Boolean): Single<List<PriceChart>> {
        val param = hashMapOf<String, String>("start" to startTime.toString(),
                "end" to "9999999999",
                "period" to period.toString(),
                "currencyPair" to (if (usd) "USDT_ETH" else "BTC_ETH"))
        return RetrofitManager.retrofit().create(APIService::class.java).getPriceChart(param)
                .subscribeOn(Schedulers.io())
                .map { t ->
                    Cache.getWithoutDisc(activity.applicationContext).put("getPriceChart", t as Serializable)
                    t
                }
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())


    }

}