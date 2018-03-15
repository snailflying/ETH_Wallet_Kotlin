package cn.mw.ethwallet.network

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import cn.mw.ethwallet.domain.api.APIService
import cn.mw.ethwallet.domain.response.*
import cn.mw.ethwallet.interfaces.StorableWallet
import cn.mw.ethwallet.utils.cache.Cache
import com.safframework.lifecycle.RxLifecycle
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.BufferedInputStream
import java.io.Serializable
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 11/03/2018 09:55
 * @description
 */
class EtherscanAPI private constructor() {


    companion object {
        val INSTANCE: EtherscanAPI by lazy { EtherscanAPI() }
        val CACHE_KEY_TOKEN_BALANCES = "getTokenBalances"
        val CACHE_KEY_INTERNAL_TRANSACTIONS = "getInternalTransactions"
        val CACHE_KEY_TRANSACTIONS = "getNormalTransactions"
    }

    fun getEtherPrice(activity: AppCompatActivity): Single<EtherPrice> {
        return RetrofitManager.retrofit().create(APIService::class.java).getEtherPrice()
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())
    }


    fun getInternalTransactions(activity: AppCompatActivity, address: String, force: Boolean): Single<String> {
        if (!force && Cache.getInstanceWithoutDisc(activity).getObject(CACHE_KEY_INTERNAL_TRANSACTIONS) != null) {
            return Single.just(Cache.getInstanceWithoutDisc(activity).getString(CACHE_KEY_INTERNAL_TRANSACTIONS))

        }
        return RetrofitManager.retrofit().create(APIService::class.java).getInternalTransactions(address)
                .map { t ->
                    Cache.getInstanceWithoutDisc(activity.applicationContext).put(CACHE_KEY_INTERNAL_TRANSACTIONS, t)
                    t
                }
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNormalTransactions(activity: AppCompatActivity, address: String, force: Boolean): Single<String> {
        if (!force && Cache.getInstanceWithoutDisc(activity).getObject(CACHE_KEY_TRANSACTIONS) != null) {
            return Single.just(Cache.getInstanceWithoutDisc(activity).getString(CACHE_KEY_TRANSACTIONS))

        }
        return RetrofitManager.retrofit().create(APIService::class.java).getNormalTransactions(address)
                .map { t ->
                    Cache.getInstanceWithoutDisc(activity.applicationContext).put(CACHE_KEY_TRANSACTIONS, t)
                    t
                }
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadTokenIcon(activity: AppCompatActivity, tokenName: String) {
        var tokenName = tokenName
        if (tokenName.indexOf(" ") > 0)
            tokenName = tokenName.substring(0, tokenName.indexOf(" "))
        if (TokenIconCache.getInstance(activity).contains(tokenName)) return

        if (tokenName.equals("OMGToken", ignoreCase = true))
            tokenName = "omise"
        else if (tokenName.equals("0x", ignoreCase = true))
            tokenName = "0xtoken_28"

        val tokenNamef = tokenName
        RetrofitManager.retrofit().create(APIService::class.java).loadTokenIcon(tokenNamef)
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    object : SingleObserver<ResponseBody> {
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onSuccess(t: ResponseBody) {
                            val `in` = t
                            val inputStream = `in`.byteStream()
                            val bufferedInputStream = BufferedInputStream(inputStream)
                            val bitmap = BitmapFactory.decodeStream(bufferedInputStream)
                            TokenIconCache.getInstance(activity).put(activity, tokenNamef, BitmapDrawable(activity.resources, bitmap).bitmap)
                        }

                        override fun onError(e: Throwable) {
                        }

                    }
                }, {})
    }

    fun getGasLimitEstimate(activity: AppCompatActivity, to: String): Single<GasPrice> {
        return RetrofitManager.retrofit().create(APIService::class.java).getGasLimitEstimate(to)
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getBalance(activity: AppCompatActivity, address: String): Single<Balance> {
        return RetrofitManager.retrofit().create(APIService::class.java).getBalance(address)
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun getNonceForAddress(address: String): Single<NonceForAddress> {
        return RetrofitManager.retrofit().create(APIService::class.java).getNonceForAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun getBalances(addresses: ArrayList<StorableWallet>): Single<String> {
        var queryMap = ""
        for (address in addresses)
            queryMap += address.pubKey + ","

        return RetrofitManager.retrofit().create(APIService::class.java).getBalances(queryMap.substring(0, queryMap.length - 1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun forwardTransaction(raw: String): Single<ForwardTX> {
        return RetrofitManager.retrofit().create(APIService::class.java).forwardTransaction(raw)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }


    fun getPriceChart(startTime: Long, period: Int, usd: Boolean): Single<List<PriceChart>> {

        val param = hashMapOf<String, String>("start" to startTime.toString(),
                "end" to "9999999999",
                "period" to period.toString(),
                "currencyPair" to (if (usd) "USDT_ETH" else "BTC_ETH"))
        return RetrofitManager.retrofit().create(APIService::class.java).getPriceChart(param)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPriceConversionRates(currencyConversion: String): Single<Rate> {
        return RetrofitManager.retrofit().create(APIService::class.java).getPriceConversionRates(currencyConversion)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTokenBalances(activity: AppCompatActivity, address: String, force: Boolean): Single<List<TokenDisplay>> {
        if (!force && Cache.getInstanceWithoutDisc(activity).getObject(CACHE_KEY_TOKEN_BALANCES) != null) {
            return Single.just(Cache.getInstanceWithoutDisc(activity, CACHE_KEY_TOKEN_BALANCES).getObject(CACHE_KEY_TOKEN_BALANCES) as List<TokenDisplay>)

        }
        return RetrofitManager.retrofit().create(APIService::class.java).getTokenBalances(address)
                .map { t ->
                    Cache.getInstanceWithoutDisc(activity.applicationContext).put(CACHE_KEY_TOKEN_BALANCES, t as Serializable)
                    t
                }
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycle.bind(activity).toLifecycleTransformer())
                .observeOn(AndroidSchedulers.mainThread())
    }

}