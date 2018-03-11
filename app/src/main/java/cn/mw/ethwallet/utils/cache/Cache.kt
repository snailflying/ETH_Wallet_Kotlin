package cn.mw.ethwallet.utils.cache

import android.content.Context

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 11/03/2018 19:04
 * @description
 */
object Cache {

    /**
     * 默认的缓存名称为Cache
     *
     * @param context
     * @return
     */
    operator fun get(context: Context): CacheApi {
        return get(context, "Cache", true)
    }

    fun getWithoutDisc(context: Context): CacheApi {
        return get(context, "Cache", false)
    }

    operator fun get(context: Context, cacheName: String): CacheApi {
        return get(context, cacheName, true)
    }

    fun getWithoutDisc(context: Context, cacheName: String): CacheApi {
        return get(context, cacheName, false)
    }

    private operator fun get(context: Context, cacheName: String, withDisc: Boolean): CacheApi {
        return CacheProxy.get(context, cacheName, withDisc)
    }

}