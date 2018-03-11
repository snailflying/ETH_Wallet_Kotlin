package cn.mw.ethwallet.utils

import android.content.Context
import android.support.v7.app.AppCompatActivity
import cn.mw.ethwallet.utils.cache.Cache
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 07/11/2017 9:09 PM
 * @description
 */
object RxJavaUtils {

    @JvmStatic
    fun <T> observableToMain(): ObservableTransformer<T, T> {

        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    @JvmStatic
    fun <T> flowableToMain(): FlowableTransformer<T, T> {

        return FlowableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    @JvmStatic
    fun <T> singleToMain(): SingleTransformer<T, T> {

        return SingleTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    @JvmStatic
    fun completableToMain(): CompletableTransformer {

        return CompletableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    @JvmStatic
    fun <T> maybeToMain(): MaybeTransformer<T, T> {

        return MaybeTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 防止重复点击的Transformer
     */
    @JvmStatic
    fun <T> preventDuplicateClicksTransformer(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.throttleFirst(1000, TimeUnit.MILLISECONDS)
        }
    }

    /**
     * 防止重复点击的Transformer
     */
    @JvmStatic
    fun <T> preventDuplicateClicksTransformer(windowDuration: Long, timeUnit: TimeUnit): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.throttleFirst(windowDuration, timeUnit)
        }
    }

    /**
     * 针对key做缓存
     * map将Maybe<T>扒光为T
     */
    @JvmStatic
    fun <T> toCacheTransformer(context: AppCompatActivity, key: String): SingleTransformer<T, T> {

        return SingleTransformer { upstream ->
            upstream.map { t ->
                Cache.get(context.applicationContext).put(key, t as Serializable)
                t
            }
//            Cache.get(context.applicationContext).put(key, upstream as Serializable)
//            upstream
        }
    }

    @JvmStatic
    fun <T> toMemCacheTransformer(context: Context, key: String): SingleTransformer<T, T> {

        return SingleTransformer { upstream ->
            //            Cache.get(context.applicationContext).put(key, upstream as Serializable)
//            upstream
            upstream.map { t ->
                Cache.getWithoutDisc(context.applicationContext).put(key, t as Serializable)
                t
            }

        }
    }
}