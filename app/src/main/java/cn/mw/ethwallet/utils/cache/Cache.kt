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
    fun getInstance(context: Context): CacheApi {
        return getInstance(context, "Cache", true)
    }

    fun getInstanceWithoutDisc(context: Context): CacheApi {
        return getInstance(context, "Cache", false)
    }

    fun getInstance(context: Context, cacheName: String): CacheApi {
        return getInstance(context, cacheName, true)
    }

    fun getInstanceWithoutDisc(context: Context, cacheName: String): CacheApi {
        return getInstance(context, cacheName, false)
    }

    private fun getInstance(context: Context, cacheName: String, withDisc: Boolean): CacheApi {
        return CacheProxy.get(context, cacheName, withDisc)
    }

}